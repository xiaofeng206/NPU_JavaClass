public class Matrix {

    public static void main(String[] args) {
        // 定義兩個範例矩陣 A (2x3) 與 B (3x2)
        // 注意：A 的欄數 (columns) 必須等於 B 的列數 (rows)
        double[][] matrixA = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        };

        double[][] matrixB = {
            {7.0, 8.0},
            {9.0, 10.0},
            {11.0, 12.0}
        };

        // 執行矩陣乘法
        double[][] resultC = multiplyMatrices(matrixA, matrixB);

        // 輸出結果
        if (resultC != null) {
            System.out.println("矩陣 A 與 B 相乘的結果為：");
            printMatrix(resultC);
        }
    }

    /**
     * 矩陣乘法核心邏輯
     * @param a 矩陣 A
     * @param b 矩陣 B
     * @return 相乘後的矩陣 C
     */
    public static double[][] multiplyMatrices(double[][] a, double[][] b) {
        int rowsA = a.length;         // A 的行數 (i)
        int colsA = a[0].length;      // A 的列數 (k)
        int rowsB = b.length;         // B 的行數 (k)
        int colsB = b[0].length;      // B 的列數 (j)

        // 檢查矩陣是否符合相乘條件：A 的列數必須等於 B 的行數
        if (colsA != rowsB) {
            System.out.println("錯誤：矩陣維度不匹配，無法相乘！");
            return null;
        }

        // 初始化結果矩陣 C，大小為 [A的行數] x [B的列數]
        double[][] c = new double[rowsA][colsB];

        // 三層迴圈實現公式：C[i][j] = Σ (A[i][k] * B[k][j])
        for (int i = 0; i < rowsA; i++) {           // 遍歷 A 的每一列
            for (int j = 0; j < colsB; j++) {       // 遍歷 B 的每一行
                for (int k = 0; k < colsA; k++) {   // 進行累加計算 (Σ)
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return c;
    }

    // 輔助方法：列印矩陣
    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double val : row) {
                System.out.printf("%.2f  ", val);
            }
            System.out.println();
        }
    }
}