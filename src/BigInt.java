import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BigInt implements Comparable<BigInt> {

    private final List<Integer> digits;
    private boolean sign = true;

    private BigInt(final List<Integer> digits, final boolean sign) {
        this.digits = digits;
        this.sign = sign;
    }


    public static BigInt valueOf(long num) {
        return new BigInt(String.valueOf(num));
    }

    public BigInt(int number) {
        this(String.valueOf(number));
    }

    public BigInt(String str) {
        digits = new ArrayList<>();

        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);

            if (i == 0 && c == '-') {
                sign = false;
                continue;
            }

            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException(str + " это не int");
            }

            digits.add(Character.digit(c, 10));
        }
    }

    public BigInt add(final BigInt other) {
        final List<Integer> finalDigits = new ArrayList<>();
        int remain = 0;
//одинаковые знаки
        if (this.sign == other.sign) {
            final int maxLength = Math.max(size(), other.size());
            for (int i = 0; i < maxLength; i++) {
                if (this.digits.size() >= i + 1 && other.digits.size() >= i + 1) {//есть обе цифры
                    int sum = digitAt(i) + other.digitAt(i) + remain;
                    int digit = sum % 10;
                    remain = sum / 10;
                    finalDigits.add(digit);
                } else {
                    if (other.digits.size() < i + 1) {// у второго числа закончились цифры
                        int sum = digitAt(i) + remain;
                        int digit = sum % 10;
                        remain = sum / 10;
                        finalDigits.add(digit);
                    } else {
                        int sum = other.digitAt(i) + remain;// у первого числа закончились цифры
                        int digit = sum % 10;
                        remain = sum / 10;
                        finalDigits.add(digit);
                    }
                }
            }
            if (remain != 0) {
                finalDigits.add(remain);
            }
            for (int i = finalDigits.size() - 1; i >= 0; i--) {
                if (finalDigits.get(i) == 0) {
                    finalDigits.remove(i);
                } else {
                    break;
                }
            }
            return new BigInt(finalDigits, this.sign);

        } else {// СЛОЖЕНИЕ ЧИСЕЛ С РАЗНЫМИ ЗНАКАМИ
            final int maxLength = Math.max(size(), other.size());
            if (this.compareToAbs(other) == 1 || this.compareToAbs(other) == 0) {// наше по модулю больше
                remain = 0;

                for (int i = 0; i < maxLength; i++) {
                    if (this.digits.size() >= i + 1 && other.digits.size() >= i + 1) {//есть обе цифры
                        int sum = digitAt(i) - other.digitAt(i) + remain;
                        if (sum < 0) {
                            sum += 10;
                            remain = -1;
                        } else {
                            remain = 0;
                        }
                        finalDigits.add(sum);
                    } else {
                        if (other.digits.size() <= i + 1) {// у второго числа закончились цифры
                            int sum = this.digitAt(i) + remain;
                            if (sum < 0) {
                                sum += 10;
                                remain = -1;
                            } else {
                                remain = 0;
                            }
                            finalDigits.add(sum);
                        }

                    }
                }
                for (int i = finalDigits.size() - 1; i >= 0; i--) {
                    if (finalDigits.get(i) == 0) {
                        finalDigits.remove(i);
                    } else {
                        break;
                    }
                }
                return new BigInt(finalDigits, this.sign);
            } else {//другое по модулю больше
                return other.add(this);
            }

        }
    }

    public BigInt subtract(final BigInt other) {
        BigInt newOther = new BigInt(other.digits, !other.sign);
        return this.add(newOther);
    }


    public BigInt multiply(BigInt other) {
        final int finalSize = size() + other.size();
        final List<Integer> finalDigits = new ArrayList<>(Collections.nCopies(finalSize, 0));
        final boolean finalSign = sign == other.sign;

        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < other.size(); j++) {
                int prev = finalDigits.get(i + j);
                finalDigits.set(i + j, prev + digitAt(i) * other.digitAt(j));
            }
        }

        int remain = 0;
        for (int i = 0; i < finalSize; i++) {
            int s = remain + finalDigits.get(i);
            finalDigits.set(i, s % 10);
            remain = s / 10;
        }
        for (int i = finalDigits.size() - 1; i >= 0; i--) {
            if (finalDigits.get(i) == 0) {
                finalDigits.remove(i);
            } else {
                break;
            }
        }
        return new BigInt(finalDigits, finalSign);
    }

    public BigInt divide(BigInt other) {
        if (this.compareToAbs(other) == -1 || other.compareTo(new BigInt(0)) == 0) {
            return new BigInt(0);
        }
        BigInt newOther = new BigInt(other.digits, true);

        final List<Integer> finalDigits = new ArrayList<>();
        BigInt t = new BigInt(digitAt(this.size() - 1));
        int i = this.size() - 2;
        for (; t.compareToAbs(newOther) == -1; i--) {
            t = t.multiply(new BigInt(10)).add(new BigInt(this.digitAt(i)));
        }
        int cc = 9;
        int b = t.size();
        for (; b <= size(); b++) {
            for (; (new BigInt(cc)).multiply(newOther).compareToAbs(t) > 0; cc--) {
                int a = 0;
            }
            t = t.subtract((new BigInt(cc)).multiply(newOther));
            finalDigits.add(0, cc);
            if (i < 0) {
                return new BigInt(finalDigits, this.sign == other.sign);
            }
            t = t.multiply(new BigInt(10)).add(new BigInt(digitAt(i)));
            i--;
            cc = 9;
        }
        return new BigInt(finalDigits, this.sign == other.sign);
    }


    @Override
    public int compareTo(BigInt other) {
        if (sign && !other.sign) {
            return 1;
        } else if (other.sign && !sign) {
            return -1;
        }

        if (size() > other.size()) {
            return sign ? 1 : -1;
        } else if (other.size() > size()) {
            return other.sign ? -1 : 1;
        }

        for (int i = size() - 1; i >= 0; i--) {
            int we = digitAt(i);
            int them = other.digitAt(i);
            if (we > them) {
                return sign ? 1 : -1;
            } else if (them > we) {
                return other.sign ? -1 : 1;
            }
        }

        return 0;
    }

    public int compareToAbs(BigInt other) {
        if (size() == other.size()) {
            for (int i = size() - 1; i >= 0; i--) {
                int we = digitAt(i);
                int them = other.digitAt(i);
                if (we > them) {
                    return 1;
                } else if (them > we) {
                    return -1;
                }
            }
        }
        if (size() > other.size()) {
            return 1;
        } else {
            if (size() < other.size()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean seenNonZeroDigit = false;
        if (!sign) {
            sb.append('-');
        }

        for (int i = size() - 1; i >= 0; i--) {
            int digit = digits.get(i);

            if (digit != 0 || seenNonZeroDigit) {
                sb.append(digit);
            }

            if (digit != 0) {
                seenNonZeroDigit = true;
            }
        }

        if (!seenNonZeroDigit) {
            return "0";
        }

        return sb.toString();
    }

    private int size() {
        return digits.size();
    }

    private int digitAt(final int i) {
        if (i >= digits.size()) {
            return 0;
        }

        return digits.get(i);
    }
}