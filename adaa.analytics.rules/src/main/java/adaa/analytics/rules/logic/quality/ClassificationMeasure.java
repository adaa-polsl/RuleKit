package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.utils.compiler.CompilerUtils;

import com.rapidminer.operator.OperatorException;

import java.io.*;
import java.util.logging.Level;

public class
ClassificationMeasure implements IQualityMeasure {

    public static final int Accuracy = 0;
    public static final int BinaryEntropy = 1;
    public static final int C1 = 2;
    public static final int C2 = 3;
    public static final int CFoil = 4;
    public static final int CN2Significnce = 5;
    public static final int Correlation = 6;
    public static final int Coverage = 7;
    public static final int FBayesianConfirmation = 8;
    public static final int FMeasure = 9;
    public static final int FullCoverage = 10;
    public static final int GeoRSS = 11;
    public static final int GMeasure = 12;
    public static final int InformationGain = 13;
    public static final int JMeasure = 14;
    public static final int Kappa = 15;
    public static final int Klosgen = 16;
    public static final int Laplace = 17;
    public static final int Lift = 18;
    public static final int LogicalSufficiency = 19;
    public static final int MEstimate = 20;
    public static final int MutualSupport = 21;
    public static final int Novelty = 22;
    public static final int OddsRatio = 23;
    public static final int OneWaySupport = 24;
    public static final int PawlakDependencyFactor = 25;
    public static final int Q2 = 26;
    public static final int Precision = 27;
    public static final int RelativeRisk = 28;
    public static final int Ripper = 29;
    public static final int RuleInterest = 30;
    public static final int RSS = 31;
    public static final int SBayesian = 32;
    public static final int Sensitivity = 33;
    public static final int Specificity = 34;
    public static final int TwoWaySupport = 35;
    public static final int WeightedLaplace = 36;
    public static final int WeightedRelativeAccuracy = 37;
    public static final int YAILS = 38;
    public static final int UserDefined = 39;

    public static final int COUNT = 40;

    private  IUserMeasure userMeasure;
    protected int criterion = Correlation;

    public ClassificationMeasure(int criterion) {
        this.criterion = criterion;
    }

    public String getName() {
        return ClassificationMeasure.getName(criterion);
    }

    public static String getName(int criterion) {
        switch (criterion) {
            case Accuracy:
                return "Accuracy";
            case BinaryEntropy:
                return "BinaryEntropy";
            case C1:
                return "C1";
            case C2:
                return "C2";
            case CFoil:
                return "CFoil";
            case CN2Significnce:
                return "CN2Significance";
            case Correlation:
                return "Correlation";
            case Coverage:
                return "Coverage";
            case FBayesianConfirmation:
                return "FBayesianConfirmation";
            case FMeasure:
                return "FMeasure";
            case FullCoverage:
                return "FullCoverage";
            case GeoRSS:
                return "GeoRSS";
            case GMeasure:
                return "GMeasure";
            case InformationGain:
                return "InformationGain";
            case JMeasure:
                return "JMeasure";
            case Kappa:
                return "Kappa";
            case Klosgen:
                return "Klosgen";
            case Laplace:
                return "Laplace";
            case Lift:
                return "Lift";
            case LogicalSufficiency:
                return "LogicalSufficiency";
            case MEstimate:
                return "MEstimate";
            case MutualSupport:
                return "MutualSupport";
            case Novelty:
                return "Novelty";
            case OddsRatio:
                return "OddsRatio";
            case OneWaySupport:
                return "OneWaySupport";
            case PawlakDependencyFactor:
                return "PawlakDependencyFactor";
            case Q2:
                return "Q2";
            case Precision:
                return "Precision";
            case RelativeRisk:
                return "RelativeRisk";
            case Ripper:
                return "Ripper";
            case RuleInterest:
                return "RuleInterest";
            case RSS:
                return "RSS";
            case SBayesian:
                return "SBayesian";
            case Sensitivity:
                return "Sensitivity";
            case Specificity:
                return "Specificity";
            case TwoWaySupport:
                return "TwoWaySupport";
            case WeightedLaplace:
                return "WeightedLaplace";
            case WeightedRelativeAccuracy:
                return "WeightedRelativeAccuracy";
            case YAILS:
                return "Yails";
            case UserDefined:
                return "UserDefined";
            default:
                throw new IllegalArgumentException("ClassificationMeasure: unknown measure type");
        }
    }

    public double calculate(double p, double n, double P, double N) {
        return calculate(p, n, P, N, this.criterion);
    }

    public double calculate(double p, double n, double P, double N, int criterion) {
        switch (criterion) {
            case Accuracy:
                return p - n;

            case BinaryEntropy:
                double probs[][] = new double[2][2];
                double H[] = new double[2];

                probs[0][0] = p / (p + n);        // covered
                probs[0][1] = 1 - probs[0][0];
                probs[1][0] = (P - p) / (P + N - p - n); // uncovered
                probs[1][1] = 1 - probs[1][0];

                H[0] = H[1] = 0;

                for (int i = 0; i < 2; ++i) {
                    for (int j = 0; j < 2; ++j) {
                        if (probs[i][j] > 0) {
                            H[i] -= probs[i][j] * Math.log(probs[i][j]) / Math.log(2.0);
                        }
                    }
                }

                double coveredFrac = (p + n) / (P + N);
                double CH = coveredFrac * H[0] + (1 - coveredFrac) * H[1];
                return 1 - CH;

            case C1:
                double cohen = calculate(p, n, P, N, Kappa);
                return ((N * p - P * n) / (N * (p + n))) * ((2.0 + cohen) / 3.0);

            case C2:
                return (((P + N) * p / (p + n) - P) / N) * ((1 + p / P) / 2);

            case CFoil:
                return p * (log2(p / (p + n)) - log2(P / (P + N)));

            case CN2Significnce:
                return 2 * (
                        p * Math.log(p / ((p + n) * P / (P + N))) +
                                n * Math.log(n / ((p + n) * N / (P + N)))
                );

            case Correlation:
                return (p * N - P * n) / Math.sqrt(P * N * (p + n) * (P - p + N - n));

            case Coverage:
                return p / P;

            case FBayesianConfirmation:
                return (p * N - n * P) / (p * N + n * P);

            case FMeasure:
                double beta_2 = 2 * 2;
                return
                        (beta_2 + 1) * (p / (p + n)) * (p / P) /
                                (beta_2 * (p / (p + n)) + p / P);

            case FullCoverage:
                return (p + n) / (P + N);

            case GeoRSS:
                return Math.sqrt(p / P * (1 - n / N));

            case GMeasure:
                double g = 2;
                return p / (p + n + g);

            case InformationGain:
                return info(P, N) - (p + n) / (P + N) * info(p, n) - (P + N - p - n) / (P + N) * info(P - p, N - n);

            case JMeasure:
                return (1.0 / (P + N)) * (
                        p * Math.log(p * (P + N) / ((p + n) * P)) +
                                n * Math.log(n * (P + N) / ((p + n) * N))
                );

            case Kappa:
                return
                        ((P + N) * (p / (p + n)) - P) /
                                ((P + N) / 2 * ((p + n + P) / (p + n)) - P);

            case Klosgen:
                double omega = 1;
                return Math.pow((p + n) / (P + N), omega) * (p / (p + n) - P / (P + N));

            case Laplace:
                return (p + 1) / (p + n + 2);

            case Lift:
                return p * (P + N) / ((p + n) * P);

            case LogicalSufficiency:
                return p * N / (n * P);

            case MEstimate:
                double m = 2;
                return (p + m * P / (P + N)) / (p + n + m);

            case MutualSupport:
                return p / (n + P);

            case Novelty:
                return p / (P + N) - (P * (p + n) / ((P + N) * (P + N)));

            case OddsRatio:
                return p * (N - n) / (n * (P - p));

            case OneWaySupport:
                return p / (p + n) * Math.log(p * (P + N) / ((p + n) * P));

            case PawlakDependencyFactor:
                return (p * (P + N) - P * (p + n)) / (p * (P + N) + P * (p + n));

            case Q2:
                return (p / P - n / N) * (1 - n / N);

            case Precision:
                return p / (p + n);

            case RelativeRisk:
                return (p / (p + n)) * ((P + N - p - n) / (P - p));

            case Ripper:
                return (p - n) / (p + n);

            case RuleInterest:
                return (p * (P + N) - (p + n) * P) / (P + N);

            case RSS:
                return p / P - n / N;

            case SBayesian:
                return p / (p + n) - (P - p) / (P - p + N - n);

            case Sensitivity:
                return p / P;

            case Specificity:
                return (N - n) / N;

            case TwoWaySupport:
                return (p / (P + N)) * Math.log((p * (P + N)) / ((p + n) * P));

            case WeightedLaplace:
                return (p + 1) * (P + N) / ((p + n + 2) * P);

            case WeightedRelativeAccuracy:
                return (p + n) / (P + N) * (p / (p + n) - P / (P + N));

            case YAILS:
                double prec = calculate(p, n, P, N, Precision);
                double w1 = 0.5 + 0.25 * prec;
                double w2 = 0.5 - 0.25 * prec;
                return w1 * p / (p + n) + w2 * (p / P);

            case UserDefined:
                userMeasure.setValues(p,n,P,N);
                return userMeasure.getResult();

            default:
                throw new IllegalArgumentException("ClassificationMeasure: unknown measure type");
        }
    }

    public double calculate(ContingencyTable ct) {
        return this.calculate(ct.weighted_p, ct.weighted_n,
        		ct.weighted_P, ct.weighted_N);
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }

    private double info(double x, double y) {
        double prob_x = x / (x + y);
        double prob_y = 1.0 - prob_x;
        return -(prob_x * log2(prob_x) + prob_y * log2(prob_y));
    }

    public void createUserMeasure(String userMeasure) throws OperatorException {

        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("adaa/analytics/rules/resources/UserMeasureTemplate.txt");
        InputStreamReader reader = new InputStreamReader(resourceAsStream);

        StringBuffer sb = new StringBuffer();
        String javaCode = "";
        String className = "adaa.analytics.rules.logic.quality.UserMeasure";
        if (resourceAsStream == null) {
            throw new OperatorException("File 'UserMeasureTemplate.txt' doesn't exist.");
        }
        try {
            BufferedReader in = new BufferedReader(reader);
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
            in.close();
            reader.close();
            resourceAsStream.close();
            String s = sb.toString();
            javaCode = s.replaceAll("(equation)", userMeasure);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OperatorException("Couldn't open file with user defined induction measure. " + e.getMessage());
        }
        double result = 0;
        try {
            Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(className, javaCode);
            IUserMeasure measure = (IUserMeasure) aClass.newInstance();
            this.userMeasure = measure;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new OperatorException("Error while compiling UserMeasure class. " + e.getMessage());
        }
    }

}
