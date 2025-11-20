package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;
import com.matfragg.creditofacil.api.model.entities.Settings;
import com.matfragg.creditofacil.api.model.enums.Capitalization;
import com.matfragg.creditofacil.api.model.enums.GracePeriodType;
import com.matfragg.creditofacil.api.model.enums.InterestRateType;
import com.matfragg.creditofacil.api.model.enums.PeriodType; 
import com.matfragg.creditofacil.api.service.FrenchMethodCalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

@Service
@Slf4j
public class FrenchMethodCalculatorServiceImpl implements FrenchMethodCalculatorService {

    private static final int SCALE = 20; // Precisión para cálculos intermedios
    private static final int MONEY_SCALE = 2; // Precisión para montos monetarios

    @Override
    public List<PaymentSchedule> calculatePaymentSchedule(
            BigDecimal amountToFinance,
            BigDecimal annualRate, // Tasa como porcentaje (ej: 9.5)
            Integer termYears,
            Settings settings,
            BigDecimal lifeInsuranceRate,
            BigDecimal propertyInsuranceAmount) {

        log.debug("Calculando cronograma de pagos para monto: {} a {} años", amountToFinance, termYears);

        Capitalization capitalization = Objects.requireNonNullElse(settings.getCapitalization(), Capitalization.MONTHLY);

        if (settings.getCapitalization() == null) {
            log.warn("Capitalization era null, asignando MONTHLY por defecto");
        }
        // Normalizar la tasa (ej: 9.5 -> 0.095)
        BigDecimal annualRateDecimal = annualRate.divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);

        // Paso 1: Convertir tasa si es nominal
        BigDecimal effectiveAnnualRate = convertToEffectiveRate(
                annualRateDecimal, // Pasa la tasa decimal
                settings.getInterestRateType(),
                capitalization
        );

        // Paso 2: Calcular TEM
        BigDecimal monthlyRate = calculateMonthlyRate(effectiveAnnualRate);
        log.debug("TEM calculada: {}%", monthlyRate.multiply(BigDecimal.valueOf(100)));

        // Paso 3: Calcular número total de pagos
        int totalMonths = termYears * 12;

        // Paso 4: Calcular cuota mensual fija (sin seguros)
        BigDecimal baseMonthlyPayment = calculateMonthlyPayment(amountToFinance, monthlyRate, totalMonths);
        log.debug("Cuota base calculada: {}", baseMonthlyPayment);

        // Paso 5: Generar cronograma inicial
        List<PaymentSchedule> schedule = generateInitialSchedule(
                amountToFinance,
                baseMonthlyPayment,
                monthlyRate,
                totalMonths,
                lifeInsuranceRate,
                propertyInsuranceAmount
        );

        // Paso 6: Aplicar periodo de gracia si existe
        if (settings.getGraceMonths() != null && settings.getGraceMonths() > 0) {
            // --- 2. PASAR LA TASA DE SEGURO ---
            applyGracePeriod(schedule, settings.getGracePeriodType(), settings.getGraceMonths(), monthlyRate, lifeInsuranceRate);
        }

        return schedule;
    }

    @Override
    public BigDecimal calculateMonthlyPayment(BigDecimal amountToFinance, BigDecimal monthlyRate, Integer totalMonths) {
        // ✅ USAR double PARA PRECISIÓN EN pow() GRANDE
        double r = monthlyRate.doubleValue();
        double pv = amountToFinance.doubleValue();
        double n = totalMonths.doubleValue();
        
        // Fórmula exacta: PV * [r(1+r)^n] / [(1+r)^n - 1]
        double powTerm = Math.pow(1.0 + r, n);
        double monthlyPayment = pv * (r * powTerm) / (powTerm - 1.0);
        
        // Dentro del método
        log.info("Cuota calculada: {} para PV: {}, r: {}, n: {}", 
                BigDecimal.valueOf(monthlyPayment), pv, r, n);
        return BigDecimal.valueOf(monthlyPayment)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal convertToEffectiveRate(
            BigDecimal rate,  // Tasa decimal (ej: 0.095)
            InterestRateType interestRateType,
            Capitalization capitalization) {
        
        InterestRateType type = (interestRateType == null) ? InterestRateType.EFFECTIVE : interestRateType;

        if (type == InterestRateType.EFFECTIVE) {
            return rate;  // Ya es TEA (0.095), devolver tal cual
        }

        // Convertir tasa nominal (TNA) a efectiva (TEA)
        int periods = getCapitalizationPeriods(capitalization);
        BigDecimal nominalPeriodic = rate.divide(BigDecimal.valueOf(periods), SCALE, RoundingMode.HALF_UP);
        double effectiveRate = Math.pow(1 + nominalPeriodic.doubleValue(), periods) - 1;
        return BigDecimal.valueOf(effectiveRate).setScale(SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateMonthlyRate(BigDecimal effectiveAnnualRate) {
        double annual = effectiveAnnualRate.doubleValue();
        double monthlyExact = Math.exp((1.0 / 12.0) * Math.log(1.0 + annual)) - 1.0;
        
        BigDecimal result = BigDecimal.valueOf(monthlyExact).setScale(SCALE, RoundingMode.HALF_UP);
        
        // 🔍 LOG PARA DEBUG
        log.debug("TEA: {}, TEM calculado exactamente: {}", effectiveAnnualRate, result);
        
        return result;
    }

    @Override
    public int getCapitalizationPeriods(Capitalization capitalization) {
        log.debug("Capitalization: {}", String.valueOf(capitalization));  // ✅ CAMBIAR A String.valueOf()
        if (capitalization == null) return 12; // Default a mensual si es nulo
        return switch (capitalization) {
            case DAILY -> 360;
            case FORTNIGHTLY -> 24;
            case MONTHLY -> 12;
            case BIMONTHLY -> 6;
            case TRIMESTERLY -> 4;
            case QUARTERLY -> 3;
            case SEMIANNUAL -> 2;
            case YEARLY -> 1;
        };
    }

    // --- 3. AÑADIR @Override Y LA FIRMA CORRECTA ---
    @Override
    public void applyGracePeriod(
        List<PaymentSchedule> schedule,
        GracePeriodType gracePeriodType,
        Integer graceMonths,
        BigDecimal monthlyRate,
        BigDecimal lifeInsuranceRate) {

    log.debug("Aplicando periodo de gracia: {} por {} meses", gracePeriodType, graceMonths);
    
    if (gracePeriodType == null || gracePeriodType == GracePeriodType.NONE) {
        return;
    }

    // --- CORRECCIÓN: Obtener el principal original ---
    BigDecimal principal = schedule.get(0).getInitialBalance();

    for (int i = 0; i < Math.min(graceMonths, schedule.size()); i++) {
        PaymentSchedule payment = schedule.get(i);
        
        // --- CORRECCIÓN: Para gracia, initialBalance siempre es el principal ---
        payment.setInitialBalance(principal);
        
        BigDecimal interest = principal.multiply(monthlyRate)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        
        // Recalcular seguro de vida con el principal
        BigDecimal lifeInsurancePayment = principal.multiply(lifeInsuranceRate)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
        payment.setLifeInsurance(lifeInsurancePayment);
        payment.setPropertyInsurance(payment.getPropertyInsurance()); // Ya está calculado

        if (gracePeriodType == GracePeriodType.TOTAL) {
            interest = interest.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            payment.setInterest(interest); 
            payment.setPrincipal(BigDecimal.ZERO);
            payment.setPayment(BigDecimal.ZERO);
            
            // Para total: saldo capitaliza intereses
            BigDecimal newBalance = principal.add(interest);
            payment.setFinalBalance(newBalance);

            payment.setTotalPayment(
                payment.getPayment()
                    .add(payment.getLifeInsurance())
                    .add(payment.getPropertyInsurance())
            );
            payment.setPeriodType(PeriodType.TOTAL_GRACE); 

        } else if (gracePeriodType == GracePeriodType.PARTIAL) {
            interest = interest.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            payment.setInterest(interest);
            payment.setPrincipal(BigDecimal.ZERO);
            payment.setPayment(interest); 
            
            // --- CORRECCIÓN: Para parcial, saldo NO cambia ---
            payment.setFinalBalance(principal);

            payment.setTotalPayment(
                payment.getPayment()
                    .add(payment.getLifeInsurance())
                    .add(payment.getPropertyInsurance())
            );
            payment.setPeriodType(PeriodType.PARTIAL_GRACE);
        }
    }

    // --- CORRECCIÓN: Recalcular con el principal correcto ---
    if (graceMonths > 0) {
        recalculateAfterGrace(schedule, graceMonths, monthlyRate, lifeInsuranceRate);
    }
}
    
    @Override
    public List<PaymentSchedule> generateInitialSchedule(
            BigDecimal amountToFinance,
            BigDecimal annualRate, // ← VIENE COMO PORCENTAJE (7.8)
            Integer termYears,
            BigDecimal lifeInsuranceRate,
            BigDecimal propertyInsurance,
            Capitalization capitalization) {
        
        // ✅ CORRECCIÓN: Normalizar ANTES de convertir
        BigDecimal annualRateDecimal = annualRate.divide(
            BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP
        );
        
        InterestRateType type = (capitalization == null) ? InterestRateType.EFFECTIVE : InterestRateType.NOMINAL;
        BigDecimal effectiveAnnualRate = convertToEffectiveRate(annualRateDecimal, type, capitalization);
        
        BigDecimal monthlyRate = calculateMonthlyRate(effectiveAnnualRate);
        int totalMonths = termYears * 12;
        BigDecimal baseMonthlyPayment = calculateMonthlyPayment(amountToFinance, monthlyRate, totalMonths);

        return generateInitialSchedule(
                amountToFinance,
                baseMonthlyPayment,
                monthlyRate,
                totalMonths,
                lifeInsuranceRate,
                propertyInsurance
        );
    }

    // ============ Métodos Privados ============

    private List<PaymentSchedule> generateInitialSchedule(
        BigDecimal principal,
        BigDecimal monthlyPayment, // Cuota base (Capital + Interés)
        BigDecimal monthlyRate,
        Integer totalMonths,
        BigDecimal lifeInsuranceRate,     // Tasa (ej: 0.00045)
        BigDecimal propertyInsuranceAmount // Monto fijo (ej: 40.00)
    ) {

        List<PaymentSchedule> schedule = new ArrayList<>();
        BigDecimal balance = principal;
        LocalDate currentDate = LocalDate.now().plusMonths(1);

        for (int month = 1; month <= totalMonths; month++) {
            PaymentSchedule payment = new PaymentSchedule();
            payment.setPaymentNumber(month);
            payment.setPaymentDate(currentDate);
            payment.setInitialBalance(balance.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            
            // --- 8. USAR EL ENUM CORRECTO ---
            payment.setPeriodType(PeriodType.ORDINARY); 
            payment.setPropertyInsurance(propertyInsuranceAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

            BigDecimal interest = balance.multiply(monthlyRate)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal principalPayment = monthlyPayment.subtract(interest);

            if (month == totalMonths) {
                principalPayment = balance;
                monthlyPayment = principalPayment.add(interest);
            }

            BigDecimal lifeInsurancePayment = balance.multiply(lifeInsuranceRate)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                    .max(BigDecimal.ZERO); 

            BigDecimal totalPayment = monthlyPayment
                    .add(lifeInsurancePayment)
                    .add(propertyInsuranceAmount);

            BigDecimal newBalance = balance.subtract(principalPayment);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }

            payment.setPayment(monthlyPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setInterest(interest);
            payment.setPrincipal(principalPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setLifeInsurance(lifeInsurancePayment); // Guardar el MONTO
            payment.setTotalPayment(totalPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setFinalBalance(newBalance.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

            schedule.add(payment);

            balance = newBalance;
            currentDate = currentDate.plusMonths(1);
        }

        return schedule;
    }
    
    // Método para recalcular después de la gracia
    private void recalculateAfterGrace(
            List<PaymentSchedule> schedule,
            Integer graceMonths,
            BigDecimal monthlyRate,
            BigDecimal lifeInsuranceRate) { // <-- 9. RECIBIR LA TASA

        if (graceMonths >= schedule.size()) {
            return;
        }

        BigDecimal newPrincipal = schedule.get(graceMonths - 1).getFinalBalance();
        int remainingMonths = schedule.size() - graceMonths;

        if (remainingMonths <= 0) {
            return;
        }

        BigDecimal newMonthlyPayment = calculateMonthlyPayment(
                newPrincipal,
                monthlyRate,
                remainingMonths
        );

        BigDecimal balance = newPrincipal;
        for (int i = graceMonths; i < schedule.size(); i++) {
            PaymentSchedule payment = schedule.get(i);
            
            BigDecimal propertyInsuranceAmount = payment.getPropertyInsurance();

            BigDecimal interest = balance.multiply(monthlyRate)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal principal_payment = newMonthlyPayment.subtract(interest);

            if (i == schedule.size() - 1) {
                principal_payment = balance;
                newMonthlyPayment = principal_payment.add(interest);
            }

            BigDecimal newBalance = balance.subtract(principal_payment);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }

            // --- 10. ARREGLO DEL BUG EXPONENCIAL ---
            // Usar la TASA (parámetro) en lugar del MONTO
            BigDecimal lifeInsurancePayment = balance.multiply(lifeInsuranceRate) 
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                    .max(BigDecimal.ZERO);

            payment.setInitialBalance(balance.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setPayment(newMonthlyPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setInterest(interest);
            payment.setPrincipal(principal_payment.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setLifeInsurance(lifeInsurancePayment); // Actualizar seguro
            
            // --- 11. USAR EL ENUM CORRECTO ---
            payment.setPeriodType(PeriodType.ORDINARY); 
            
            payment.setTotalPayment(
                newMonthlyPayment
                    .add(lifeInsurancePayment)
                    .add(propertyInsuranceAmount)
            );
            
            payment.setFinalBalance(newBalance.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

            balance = newBalance;
        }
    }
}