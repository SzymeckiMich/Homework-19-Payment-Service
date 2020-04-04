package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

class PaymentService {

    private PaymentRepository paymentRepository;
    private DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    List<Payment> findPaymentsSortedByDateDesc() {
        return paymentRepository.findAll().stream()
                .sorted(new Comparator<Payment>() {
                    @Override
                    public int compare(Payment o1, Payment o2) {
                        if (o1.getPaymentDate().getYear() > o2.getPaymentDate().getYear())
                            return -1;
                        else if (o1.getPaymentDate().getYear() < o2.getPaymentDate().getYear())
                            return 1;
                        else {
                            if (o1.getPaymentDate().getMonthValue() > o2.getPaymentDate().getMonthValue())
                                return -1;
                            else if (o1.getPaymentDate().getMonthValue() < o2.getPaymentDate().getMonthValue())
                                return 1;
                            else {
                                if (o1.getPaymentDate().getDayOfYear() > o2.getPaymentDate().getDayOfYear())
                                    return -1;
                                else if (o1.getPaymentDate().getDayOfYear() < o2.getPaymentDate().getDayOfYear())
                                    return 1;
                                else {
                                    return 0;
                                }
                            }
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getYear() == dateTimeProvider.yearMonthNow().getYear())
                .filter(payment -> payment.getPaymentDate().getMonth().equals(dateTimeProvider.yearMonthNow().getMonth()))
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getYear() == yearMonth.getYear())
                .filter(payment -> payment.getPaymentDate().getMonth().equals(yearMonth.getMonth()))
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenLastDays(int days) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getYear()==dateTimeProvider.zonedDateTimeNow().getYear())
                .filter(payment -> payment.getPaymentDate().getDayOfYear()>=dateTimeProvider.zonedDateTimeNow().getDayOfYear()-days)
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    Set<String> findProductsSoldInCurrentMonth() {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getYear() == dateTimeProvider.yearMonthNow().getYear())
                .filter(payment -> payment.getPaymentDate().getMonth().equals(dateTimeProvider.yearMonthNow().getMonth()))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        BigDecimal sum = paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getYear() == dateTimeProvider.yearMonthNow().getYear())
                .filter(payment -> payment.getPaymentDate().getMonth().equals(dateTimeProvider.yearMonthNow().getMonth()))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.valueOf(0), new BinaryOperator<BigDecimal>() {
                    @Override
                    public BigDecimal apply(BigDecimal bigDecimal, BigDecimal bigDecimal2) {
                        return bigDecimal.add(bigDecimal2);
                    }
                });
        return sum;
    }


    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        BigDecimal discounSum = paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getYear() == dateTimeProvider.yearMonthNow().getYear())
                .filter(payment -> payment.getPaymentDate().getMonth().equals(dateTimeProvider.yearMonthNow().getMonth()))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getRegularPrice)
                .reduce(BigDecimal.valueOf(0), new BinaryOperator<BigDecimal>() {
                    @Override
                    public BigDecimal apply(BigDecimal bigDecimal, BigDecimal bigDecimal2) {
                        return bigDecimal.add(bigDecimal2);
                    }
                });
        return discounSum.subtract(sumTotalForGivenMonth(yearMonth));
    }

    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithValueOver(int value) {
        throw new RuntimeException("Not implemented");
    }
}
