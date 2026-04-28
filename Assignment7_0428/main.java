import java.util.*;

/**
 * 樣本空間 (Sample Space)
 * 定義：所有可能結果的集合，記為 S
 * n(S) 表示樣本空間中的元素個數
 */
class SampleSpace {
    private Set<String> elements;
    
    public SampleSpace(Set<String> elements) {
        this.elements = new HashSet<>(elements);
    }
    
    public int getSize() {
        return elements.size();
    }
    
    public Set<String> getElements() {
        return new HashSet<>(elements);
    }
    
    @Override
    public String toString() {
        return "S = " + elements;
    }
}

/**
 * 事件類 (Event)
 * 定義：樣本空間的子集合
 * n(A) 表示事件 A 中的元素個數
 */
class Event {
    private String name;
    private Set<String> outcomes;
    
    public Event(String name, Set<String> outcomes) {
        this.name = name;
        this.outcomes = new HashSet<>(outcomes);
    }
    
    public String getName() {
        return name;
    }
    
    public int getSize() {
        return outcomes.size();
    }
    
    public Set<String> getOutcomes() {
        return new HashSet<>(outcomes);
    }
    
    @Override
    public String toString() {
        return name + " = " + outcomes;
    }
}

/**
 * 補事件類 (Complementary Event)
 * 定義：不屬於事件 A 的樣本空間中的所有元素
 * 記為 A'，且 P(A) + P(A') = 1
 */
class ComplementaryEvent extends Event {
    private Event originalEvent;
    
    public ComplementaryEvent(Event originalEvent, SampleSpace sampleSpace) {
        super(originalEvent.getName() + "'", calculateComplement(originalEvent, sampleSpace));
        this.originalEvent = originalEvent;
    }
    
    private static Set<String> calculateComplement(Event event, SampleSpace space) {
        Set<String> complement = new HashSet<>(space.getElements());
        complement.removeAll(event.getOutcomes());
        return complement;
    }
    
    public Event getOriginalEvent() {
        return originalEvent;
    }
}

/**
 * 概率類 (Probability)
 * 公式：P(A) = n(A) / n(S)
 * 其中 n(A) 是事件 A 的元素個數，n(S) 是樣本空間的元素個數
 */
class Probability {
    private SampleSpace sampleSpace;
    
    public Probability(SampleSpace sampleSpace) {
        this.sampleSpace = sampleSpace;
    }
    
    /**
     * 計算事件 A 的概率
     * P(A) = n(A) / n(S)
     */
    public double calculate(Event event) {
        if (sampleSpace.getSize() == 0) {
            throw new IllegalArgumentException("樣本空間不能為空");
        }
        return (double) event.getSize() / sampleSpace.getSize();
    }
    
    /**
     * 計算補事件 A' 的概率
     * P(A') = 1 - P(A)
     */
    public double calculateComplement(Event event) {
        return 1.0 - calculate(event);
    }
    
    /**
     * 驗證概率是否介於 0 和 1 之間
     */
    public boolean isValidProbability(double prob) {
        return prob >= 0.0 && prob <= 1.0;
    }
}

/**
 * (Union Event - Or)
 * 定義：事件 A 或事件 B 發生的所有結果
 * 記為 A∪B，P(A∪B) = P(A) + P(B) - P(A∩B)
 */
class OrEvent extends Event {
    private Event eventA;
    private Event eventB;
    
    public OrEvent(Event eventA, Event eventB) {
        super(eventA.getName() + " ∪ " + eventB.getName(), calculateUnion(eventA, eventB));
        this.eventA = eventA;
        this.eventB = eventB;
    }
    
    private static Set<String> calculateUnion(Event a, Event b) {
        Set<String> union = new HashSet<>(a.getOutcomes());
        union.addAll(b.getOutcomes());
        return union;
    }
    
    public Event getEventA() {
        return eventA;
    }
    
    public Event getEventB() {
        return eventB;
    }
}

/**
 * (Intersection Event - And)
 * 定義：事件 A 且事件 B 同時發生的所有結果
 * 記為 A∩B
 */
class AndEvent extends Event {
    private Event eventA;
    private Event eventB;
    
    public AndEvent(Event eventA, Event eventB) {
        super(eventA.getName() + " ∩ " + eventB.getName(), calculateIntersection(eventA, eventB));
        this.eventA = eventA;
        this.eventB = eventB;
    }
    
    private static Set<String> calculateIntersection(Event a, Event b) {
        Set<String> intersection = new HashSet<>(a.getOutcomes());
        intersection.retainAll(b.getOutcomes());
        return intersection;
    }
    
    public Event getEventA() {
        return eventA;
    }
    
    public Event getEventB() {
        return eventB;
    }
}

/**
 * 條件機率類 (Conditional Probability)
 * 定義：在事件 B 發生的條件下，事件 A 發生的機率
 * 公式：P(A|B) = P(A∩B) / P(B)
 */
class ConditionalProbability {
    private Probability probability;
    
    public ConditionalProbability(Probability probability) {
        this.probability = probability;
    }
    
    /**
     * 計算條件機率 P(A|B)
     * P(A|B) = P(A∩B) / P(B)
     */
    public double calculate(Event eventA, Event eventB) {
        double probB = probability.calculate(eventB);
        if (probB == 0) {
            throw new IllegalArgumentException("P(B) 不能為 0");
        }
        
        // 計算 A∩B
        AndEvent intersection = new AndEvent(eventA, eventB);
        double probIntersection = probability.calculate(intersection);
        
        return probIntersection / probB;
    }
    
    /**
     * 顯示條件機率的計算過程
     */
    public String showCalculation(Event eventA, Event eventB) {
        AndEvent intersection = new AndEvent(eventA, eventB);
        double probIntersection = probability.calculate(intersection);
        double probB = probability.calculate(eventB);
        double result = calculate(eventA, eventB);
        
        return String.format("P(%s|%s) = P(%s∩%s) / P(%s) = %.4f / %.4f = %.4f",
                eventA.getName(), eventB.getName(),
                eventA.getName(), eventB.getName(),
                eventB.getName(),
                probIntersection, probB, result);
    }
}

/**
 * 貝氏定理類 (Bayes' Theorem)
 * 公式：P(A|B) = P(B|A) × P(A) / P(B)
 * 用途：已知 B 發生的情況下，推測 A 發生的機率
 */
class BayesTheorem {
    private Probability probability;
    private ConditionalProbability conditionalProbability;
    
    public BayesTheorem(Probability probability, ConditionalProbability conditionalProbability) {
        this.probability = probability;
        this.conditionalProbability = conditionalProbability;
    }
    
    /**
     * 計算貝氏定理
     * P(A|B) = P(B|A) × P(A) / P(B)
     */
    public double calculate(Event eventA, Event eventB) {
        double probA = probability.calculate(eventA);
        double probB = probability.calculate(eventB);
        
        if (probB == 0) {
            throw new IllegalArgumentException("P(B) 不能為 0");
        }
        
        // 計算 P(B|A)
        double probBGivenA = conditionalProbability.calculate(eventB, eventA);
        
        return (probBGivenA * probA) / probB;
    }
    
    /**
     * 顯示貝氏定理的計算過程
     */
    public String showCalculation(Event eventA, Event eventB) {
        double probA = probability.calculate(eventA);
        double probB = probability.calculate(eventB);
        double probBGivenA = conditionalProbability.calculate(eventB, eventA);
        double result = calculate(eventA, eventB);
        
        return String.format(
                "貝氏定理：P(%s|%s)\n" +
                "= P(%s|%s) × P(%s) / P(%s)\n" +
                "= %.4f × %.4f / %.4f\n" +
                "= %.4f",
                eventA.getName(), eventB.getName(),
                eventB.getName(), eventA.getName(),
                eventA.getName(), eventB.getName(),
                probBGivenA, probA, probB,
                result);
    }
}

/**
 * 全機率類 (Law of Total Probability)
 * 公式：如果 B1, B2, ..., Bn 是樣本空間的分割，則
 * P(A) = Σ P(A|Bi) × P(Bi)
 * 用途：計算複雜事件的機率
 */
class TotalProbability {
    private Probability probability;
    private ConditionalProbability conditionalProbability;
    
    public TotalProbability(Probability probability, ConditionalProbability conditionalProbability) {
        this.probability = probability;
        this.conditionalProbability = conditionalProbability;
    }
    
    /**
     * 計算全機率
     * P(A) = Σ P(A|Bi) × P(Bi) for all i
     */
    public double calculate(Event eventA, Event[] partitions) {
        double totalProb = 0.0;
        
        // 驗證分割是否有效（所有分割事件不相交且並集為全集）
        for (int i = 0; i < partitions.length; i++) {
            for (int j = i + 1; j < partitions.length; j++) {
                // 檢查是否相交
                Set<String> intersection = new HashSet<>(partitions[i].getOutcomes());
                intersection.retainAll(partitions[j].getOutcomes());
                if (!intersection.isEmpty()) {
                    throw new IllegalArgumentException("分割事件之間必須互斥");
                }
            }
        }
        
        // 計算全機率
        for (Event partition : partitions) {
            double probPartition = probability.calculate(partition);
            if (probPartition > 0) {
                double probAGivenPartition = conditionalProbability.calculate(eventA, partition);
                totalProb += probAGivenPartition * probPartition;
            }
        }
        
        return totalProb;
    }
    
    /**
     * 顯示全機率的計算過程
     */
    public String showCalculation(Event eventA, Event[] partitions) {
        StringBuilder sb = new StringBuilder();
        sb.append("全機率：P(").append(eventA.getName()).append(")\n");
        sb.append("= Σ P(").append(eventA.getName()).append("|Bi) × P(Bi)\n");
        sb.append("= ");
        
        double totalProb = 0.0;
        for (int i = 0; i < partitions.length; i++) {
            double probPartition = probability.calculate(partitions[i]);
            if (probPartition > 0) {
                double probAGivenPartition = conditionalProbability.calculate(eventA, partitions[i]);
                sb.append(String.format("%.4f × %.4f", probAGivenPartition, probPartition));
                if (i < partitions.length - 1) {
                    sb.append(" + ");
                }
                totalProb += probAGivenPartition * probPartition;
            }
        }
        
        sb.append(String.format("\n= %.4f", totalProb));
        return sb.toString();
    }
}

/**
 * 獨立事件類 (Independent Events)
 * 定義：如果 P(A∩B) = P(A) × P(B)，則事件 A 和 B 獨立
 * 也等價於：P(A|B) = P(A) 或 P(B|A) = P(B)
 */
class IndependentEvents {
    private Event eventA;
    private Event eventB;
    private Probability probability;
    
    public IndependentEvents(Event eventA, Event eventB, 
                            Probability probability) {
        this.eventA = eventA;
        this.eventB = eventB;
        this.probability = probability;
    }
    
    /**
     * 判斷兩個事件是否獨立
     * 條件：P(A∩B) = P(A) × P(B)
     */
    public boolean isIndependent() {
        AndEvent intersection = new AndEvent(eventA, eventB);
        double probA = probability.calculate(eventA);
        double probB = probability.calculate(eventB);
        double probIntersection = probability.calculate(intersection);
        
        // 使用小數點誤差容差
        double expectedProduct = probA * probB;
        return Math.abs(probIntersection - expectedProduct) < 1e-10;
    }
    
    /**
     * 顯示獨立性檢驗的詳細信息
     */
    public String showVerification() {
        AndEvent intersection = new AndEvent(eventA, eventB);
        double probA = probability.calculate(eventA);
        double probB = probability.calculate(eventB);
        double probIntersection = probability.calculate(intersection);
        double product = probA * probB;
        
        String result = String.format(
                "獨立事件檢驗:\n" +
                "事件 A: %s\n" +
                "事件 B: %s\n" +
                "P(A) = %.4f\n" +
                "P(B) = %.4f\n" +
                "P(A∩B) = %.4f\n" +
                "P(A) × P(B) = %.4f\n" +
                "獨立: %s",
                eventA.getName(), eventB.getName(),
                probA, probB, probIntersection, product,
                isIndependent() ? "是" : "否");
        return result;
    }
}

class Main {
    public static void main(String[] args) {
        // 範例：擲一個骰子
        System.out.println("=== 概率論示例：擲一個骰子 ===\n");
        
        // 定義樣本空間
        Set<String> outcomes = new HashSet<>();
        outcomes.addAll(Arrays.asList("1", "2", "3", "4", "5", "6"));
        SampleSpace sampleSpace = new SampleSpace(outcomes);
        System.out.println("樣本空間：" + sampleSpace);
        System.out.println("n(S) = " + sampleSpace.getSize() + "\n");
        
        // 定義事件 A：擲出偶數
        Set<String> eventA = new HashSet<>();
        eventA.addAll(Arrays.asList("2", "4", "6"));
        Event A = new Event("A (偶數)", eventA);
        System.out.println("事件 A：" + A);
        System.out.println("n(A) = " + A.getSize());
        
        // 計算概率 P(A)
        Probability prob = new Probability(sampleSpace);
        double pA = prob.calculate(A);
        System.out.println("P(A) = n(A)/n(S) = " + A.getSize() + "/" + sampleSpace.getSize() + " = " + pA + "\n");
        
        // 定義補事件 A'：擲出奇數
        ComplementaryEvent complementA = new ComplementaryEvent(A, sampleSpace);
        System.out.println("補事件 A'：" + complementA);
        System.out.println("n(A') = " + complementA.getSize());
        
        // 計算概率 P(A')
        double pComplement = prob.calculateComplement(A);
        System.out.println("P(A') = 1 - P(A) = 1 - " + pA + " = " + pComplement + "\n");
        
        // 驗證
        System.out.println("驗證：P(A) + P(A') = " + (pA + pComplement));
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 並事件和交事件示例
        System.out.println("=== 並事件 (Or) 和交事件 (And) ===\n");
        
        // 定義事件 B：擲出大於 3 的數字
        Set<String> eventB = new HashSet<>();
        eventB.addAll(Arrays.asList("4", "5", "6"));
        Event B = new Event("B (大於3)", eventB);
        System.out.println("事件 B：" + B);
        System.out.println("P(B) = " + prob.calculate(B) + "\n");
        
        // 並事件 A∪B：偶數 或 大於3
        OrEvent orEvent = new OrEvent(A, B);
        System.out.println("並事件 A∪B：" + orEvent);
        System.out.println("P(A∪B) = " + prob.calculate(orEvent));
        System.out.println("公式驗證：P(A) + P(B) - P(A∩B) = " + 
                          (pA + prob.calculate(B) - prob.calculate(new AndEvent(A, B))) + "\n");
        
        // 交事件 A∩B：既是偶數 且 大於3
        AndEvent andEvent = new AndEvent(A, B);
        System.out.println("交事件 A∩B：" + andEvent);
        System.out.println("P(A∩B) = " + prob.calculate(andEvent) + "\n");
        
        System.out.println("=".repeat(50) + "\n");
        
        // 條件機率示例
        System.out.println("=== 條件機率 ===\n");
        
        ConditionalProbability condProb = new ConditionalProbability(prob);
        System.out.println(condProb.showCalculation(A, B));
        System.out.println("意思：在已知事件 B 發生的情況下，事件 A 也發生的機率\n");
        
        System.out.println("=".repeat(50) + "\n");
        
        // 獨立事件示例
        System.out.println("=== 獨立事件 ===\n");
        
        IndependentEvents indEvents = new IndependentEvents(A, B, prob);
        System.out.println(indEvents.showVerification());
        System.out.println("\n說明：如果 P(A∩B) ≈ P(A) × P(B)，則事件 A 和 B 獨立\n");
        
        System.out.println("=".repeat(50) + "\n");
        
        // 展示更多範例：擲兩個骰子
        System.out.println("=== 進階示例：擲兩個骰子 ===\n");
        
        Set<String> twoCoins = new HashSet<>();
        twoCoins.addAll(Arrays.asList(
            "HH", "HT", "TH", "TT"
        ));
        SampleSpace coinSpace = new SampleSpace(twoCoins);
        System.out.println("樣本空間（擲兩次硬幣）：" + coinSpace);
        
        // 事件 C：至少有一個正面
        Set<String> eventC = new HashSet<>();
        eventC.addAll(Arrays.asList("HH", "HT", "TH"));
        Event C = new Event("C (至少一個正面)", eventC);
        
        // 事件 D：第二次是反面
        Set<String> eventD = new HashSet<>();
        eventD.addAll(Arrays.asList("HT", "TT"));
        Event D = new Event("D (第二次反面)", eventD);
        
        Probability probCoin = new Probability(coinSpace);
        
        System.out.println("\n事件 C：" + C);
        System.out.println("P(C) = " + probCoin.calculate(C));
        System.out.println("\n事件 D：" + D);
        System.out.println("P(D) = " + probCoin.calculate(D));
        
        AndEvent CD = new AndEvent(C, D);
        System.out.println("\nC∩D：" + CD);
        System.out.println("P(C∩D) = " + probCoin.calculate(CD));
        
        IndependentEvents coinIndEvents = new IndependentEvents(C, D, probCoin);
        System.out.println("\n" + coinIndEvents.showVerification());
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 貝氏定理示例
        System.out.println("=== 貝氏定理 (Bayes' Theorem) ===\n");
        System.out.println("情境：工廠有 A、B 兩條生產線");
        System.out.println("已知：P(A) = 0.6, P(B) = 0.4");
        System.out.println("      P(不良品|A) = 0.05, P(不良品|B) = 0.1");
        System.out.println("求：已知取出一個不良品，它來自 A 的概率\n");
        
        // 模擬數據
        Set<String> bayesSpace = new HashSet<>();
        for (int i = 1; i <= 1000; i++) {
            if (i <= 600) {
                // A 生產線
                if (i <= 30) {
                    bayesSpace.add("A_不良" + i);
                } else {
                    bayesSpace.add("A_良好" + i);
                }
            } else {
                // B 生產線
                if (i <= 640) {
                    bayesSpace.add("B_不良" + i);
                } else {
                    bayesSpace.add("B_良好" + i);
                }
            }
        }
        SampleSpace bayesSpaceObj = new SampleSpace(bayesSpace);
        
        // 事件定義
        Set<String> fromA = new HashSet<>();
        for (String item : bayesSpace) {
            if (item.startsWith("A_")) fromA.add(item);
        }
        Event productionA = new Event("來自A線", fromA);
        
        Set<String> defective = new HashSet<>();
        for (String item : bayesSpace) {
            if (item.contains("不良")) defective.add(item);
        }
        Event defectiveEvent = new Event("不良品", defective);
        
        Probability bayesProb = new Probability(bayesSpaceObj);
        ConditionalProbability bayesCondProb = new ConditionalProbability(bayesProb);
        BayesTheorem bayes = new BayesTheorem(bayesProb, bayesCondProb);
        
        // 創建 B 生產線事件
        Set<String> fromB = new HashSet<>();
        for (String item : bayesSpace) {
            if (item.startsWith("B_")) fromB.add(item);
        }
        Event productionB = new Event("來自B線", fromB);
        
        // 計算貝氏定理：已知是不良品，來自 A 的機率
        double resultBayes = bayes.calculate(productionA, defectiveEvent);
        System.out.println(bayes.showCalculation(productionA, defectiveEvent));
        System.out.println("\n解釋：已知是不良品，來自 A 生產線的機率為 " + 
                          String.format("%.4f", resultBayes) + " 或約 " + 
                          String.format("%.2f%%", resultBayes * 100));
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 全機率示例
        System.out.println("=== 全機率 (Law of Total Probability) ===\n");
        System.out.println("情境：工廠的不良品來自 A、B 兩條生產線");
        System.out.println("已知：P(A) = 0.6, P(B) = 0.4");
        System.out.println("      P(不良品|A) = 0.05, P(不良品|B) = 0.1");
        System.out.println("求：隨機選一個產品，它是不良品的概率\n");
        
        Event[] partitions = {productionA, productionB};
        
        TotalProbability totalProb = new TotalProbability(bayesProb, bayesCondProb);
        double resultTotal = totalProb.calculate(defectiveEvent, partitions);
        System.out.println(totalProb.showCalculation(defectiveEvent, partitions));
        System.out.println("\n解釋：隨機選一個產品是不良品的機率為 " + 
                          String.format("%.4f", resultTotal) + " 或約 " + 
                          String.format("%.2f%%", resultTotal * 100));
    }
}
