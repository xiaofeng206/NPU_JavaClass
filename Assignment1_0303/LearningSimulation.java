import java.util.*;

public class LearningSimulation {
    // 參數設定
    static final int TOTAL_STEPS = 200;
    static final int FLIP_STEP = 100; // 在第 100 步時環境改變
    static final double LEARNING_RATE = 0.1; // 學習率
    static final double EPSILON = 0.2; // 探索率 (選隨機動作的機率)

    public static void main(String[] args) {
        // 模擬兩種情況：短記憶 (高忘卻) 與 長記憶 (低忘卻)
        System.out.println("=== 模擬開始：探索學習的關鍵 ===");
        
        System.out.println("\n[ 情況 A: 短記憶 (Short Memory) ]");
        runSimulation(0.5); // 忘卻率高 -> 忘恩負義

        System.out.println("\n---------------------------------");

        System.out.println("\n[ 情況 B: 長記憶 (Long Memory) ]");
        runSimulation(0.01); // 忘卻率低 -> 固執
    }

    /**
     * @param forgetRate 忘卻率 (越大學習越碎片化)
     */
    public static void runSimulation(double forgetRate) {
        // 假設有兩個選擇：A 和 B
        // 初始狀態：A 是好選擇 (1.0), B 是壞選擇 (0.0)
        double qA = 0.5, qB = 0.5; 
        double targetA = 1.0, targetB = 0.0;

        int adaptTime = -1;
        int stubbornCount = 0;
        int gratitudeCount = 0;
        int correctAfterFlip = 0;

        for (int i = 1; i <= TOTAL_STEPS; i++) {
            // 環境改變：第 100 步後，B 變成好選擇，A 變成壞選擇
            if (i == FLIP_STEP) {
                targetA = 0.0;
                targetB = 1.0;
            }

            // 選擇動作 (Epsilon-Greedy)
            int choice; // 0 為 A, 1 為 B
            if (Math.random() < EPSILON) {
                choice = (Math.random() < 0.5) ? 0 : 1;
            } else {
                choice = (qA > qB) ? 0 : 1;
            }

            // 更新 Q 值 (學習過程)
            if (choice == 0) {
                qA = qA + LEARNING_RATE * (targetA - qA);
            } else {
                qB = qB + LEARNING_RATE * (targetB - qB);
            }

            // 模擬記憶衰退 (Forgeting factor)
            qA *= (1 - forgetRate * 0.1);
            qB *= (1 - forgetRate * 0.1);

            // 指標計算邏輯
            if (i > FLIP_STEP) {
                // 1. Stubborn Score: 環境變了還選舊的好選擇 (A)
                if (choice == 0) stubbornCount++;

                // 2. Adapt Time: 環境變後，多久才開始「連續正確選擇 B」
                if (choice == 1 && adaptTime == -1) {
                    correctAfterFlip++;
                    if (correctAfterFlip > 5) adaptTime = i - FLIP_STEP;
                } else if (choice == 0) {
                    correctAfterFlip = 0;
                }
            }

            // 3. Gratitude Score: 最後步驟是否還記得最初的好選擇 A (此處簡化為最後 Q 值的殘留)
            if (i == TOTAL_STEPS) {
                gratitudeCount = (qA > 0.1) ? 1 : 0;
            }
        }

        // 輸出結果
        System.out.println("Adapt Time (適應步數): " + (adaptTime == -1 ? "未能適應" : adaptTime));
        System.out.println("Stubborn Score (固執度 %): " + ((double) stubbornCount / (TOTAL_STEPS - FLIP_STEP) * 100) + "%");
        System.out.println("Gratitude (是否留有舊情): " + (gratitudeCount == 1 ? "是 (記性好)" : "否 (忘恩負義)"));
    }
}