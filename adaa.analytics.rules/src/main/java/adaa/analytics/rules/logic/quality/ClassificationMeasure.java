/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.utils.compiler.CompilerUtils;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

import java.io.*;
import java.security.AccessControlException;

/**
 * Class gathering all quality measures for classification problems.
 *
 * @author Adam Gudys
 */
public class
ClassificationMeasure implements IQualityMeasure, Serializable {

    private static final long serialVersionUID = -2509268153427193088L;

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

    public static final String[] NAMES = {
            "Accuracy",
            "BinaryEntropy",
            "C1",
            "C2",
            "CFoil",
            "CN2Significance",
            "Correlation",
            "Coverage",
            "FBayesianConfirmation",
            "FMeasure",
            "FullCoverage",
            "GeoRSS",
            "GMeasure",
            "InformationGain",
            "JMeasure",
            "Kappa",
            "Klosgen",
            "Laplace",
            "Lift",
            "LogicalSufficiency",
            "MEstimate",
            "MutualSupport",
            "Novelty",
            "OddsRatio",
            "OneWaySupport",
            "PawlakDependencyFactor",
            "Q2",
            "Precision",
            "RelativeRisk",
            "Ripper",
            "RuleInterest",
            "RSS",
            "SBayesian",
            "Sensitivity",
            "Specificity",
            "TwoWaySupport",
            "WeightedLaplace",
            "WeightedRelativeAccuracy",
            "Yails",
            "UserDefined"
    };

    private  IUserMeasure userMeasure;
    protected int criterion = Correlation;

    public ClassificationMeasure(int criterion) {
        this.criterion = criterion;
    }

    @Override
    public String getName() {
        return ClassificationMeasure.getName(criterion);
    }

    public static String getName(int criterion) {
        if (criterion >= 0 && criterion < NAMES.length) {
            return NAMES[criterion];
        }
        else {
            throw new IllegalArgumentException("ClassificationMeasure: unknown measure type");
        }
    }

    @Override
    public double calculate(double p, double n, double P, double N) {
        return calculate(p, n, P, N, this.criterion);
    }

    @Override
    public double calculate(ExampleSet dataset, ContingencyTable ct) {
        return calculate(ct.weighted_p, ct.weighted_n,
                ct.weighted_P, ct.weighted_N, this.criterion);
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
                return (P - p + N - n == 0)
                        ? Double.NEGATIVE_INFINITY
                        : (p * N - P * n) / Math.sqrt(P * N * (p + n) * (P - p + N - n));

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
                return internalInfoGain(p, n, P, N);

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

    private double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }

    private double internalInfoGain(double p, double n, double P, double N) {
        double consequent = P;
        double notConsequent = N;

        double antecedent = p + n;
        double notAntecedent = P + N - p - n;

        double antecedentAndConsequent = p;
        double antecedentButNotConsequent = antecedent - antecedentAndConsequent;

        double notAntecedentAndNotConsequent = notConsequent - antecedentButNotConsequent;
        double notAntecedentButConsequent = notAntecedent - notAntecedentAndNotConsequent;

        double v = consequent + notConsequent;

        double a = consequent / v;
        double b = notConsequent / v;

        double infoAllExamples;
        if (b > 0)
        {
            infoAllExamples = -(a * log2(a) + b * log2(b));
        }
        else
        {
            infoAllExamples = -(a * log2(a));
        }

        double infoMatchedExamples = 0.0;
        if (antecedentAndConsequent != 0 && antecedentButNotConsequent != 0) // if rule is not accurate
        {
            a = antecedentAndConsequent / antecedent;
            b = antecedentButNotConsequent / antecedent;
            infoMatchedExamples = -(a * log2(a) + b * log2(b));
        }

        double infoNotMatchedExamples = 0.0;
        if (notAntecedentButConsequent != 0 && notAntecedentAndNotConsequent != 0)
        {
            a = notAntecedentButConsequent / notAntecedent;
            b = notAntecedentAndNotConsequent / notAntecedent;
            infoNotMatchedExamples = -(a * log2(a) + b * log2(b));
        }

        double c = antecedent / v;
        double infoRule = c * infoMatchedExamples + (1 - c) * infoNotMatchedExamples;

        double info = infoAllExamples - infoRule;

        if (antecedentButNotConsequent > 0
                && antecedentAndConsequent / antecedentButNotConsequent < consequent / notConsequent)
        {
            // this makes measure monotone
            info = -info;
        }

        assert Double.isNaN(info) == false;
        assert info <= 1.0;
        return info;
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
        } catch (ExceptionInInitializerError e) {
            if (e.getCause().getClass().getName().contains("AccessControlException")) {
                throw new OperatorException("Exception: java.security.AccessControlException occurred in 'RuleKit Generator'. " +
                        "Induction measure: 'UserDefined' is not supported for unsigned plugin. "+
                        "Please choose different induction measure. " +
                        "Read more: https://docs.rapidminer.com/latest/developers/security/",e);
            } else {
                throw (e);
            }
        }
    }
}
