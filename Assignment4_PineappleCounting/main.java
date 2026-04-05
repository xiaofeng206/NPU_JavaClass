import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class main {

    public static void main(String[] args) {
        try {
            // 讀取圖片
            File file = new File("Pineapple.jpg"); 
            BufferedImage image = ImageIO.read(file);

            // 轉灰階（考慮遠近影響）
            int width = image.getWidth();
            int height = image.getHeight();

            int[][] gray = new int[height][width];
            
            // 先計算灰階的最小最大值用於正規化
            int minGray = 255, maxGray = 0;
            int[][] rawGray = new int[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    int rgb = image.getRGB(x, y);

                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;

                    // 加權灰階轉換（更符合人眼感知）
                    int grayValue = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                    rawGray[y][x] = grayValue;
                    minGray = Math.min(minGray, grayValue);
                    maxGray = Math.max(maxGray, grayValue);
                }
            }
            
            // 考慮遠近影響的對比度增強（中心較清晰，邊緣模糊）
            int centerX = width / 2;
            int centerY = height / 2;
            double maxDistance = Math.sqrt(centerX * centerX + centerY * centerY);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    // 計算距離中心的距離
                    double distX = x - centerX;
                    double distY = y - centerY;
                    double distance = Math.sqrt(distX * distX + distY * distY);
                    double depthFactor = 1.0 - (distance / maxDistance) * 0.3; // 邊緣降低30%清晰度
                    
                    int grayValue = rawGray[y][x];
                    
                    // 正規化灰階值
                    if (maxGray != minGray) {
                        grayValue = (int)((grayValue - minGray) * 255.0 / (maxGray - minGray));
                    }
                    
                    // 應用對比度增強和深度補償
                    grayValue = (int)(128 + (grayValue - 128) * 1.3 * depthFactor);
                    
                    // 確保值在0-255範圍內
                    grayValue = Math.max(0, Math.min(255, grayValue));
                    
                    gray[y][x] = grayValue;
                }
            }

            // 邊緣偵測（水平掃描
            int horizontalCount = 0;
            int horizontalEdgePixels = 0;

            for (int y = 0; y < height; y++) {
                int edgePixels = 0;

                for (int x = 1; x < width; x++) {

                    int diff = Math.abs(gray[y][x] - gray[y][x - 1]);

                    // 動態阈值：偵測顯著邊緣
                    if (diff > 35) {
                        edgePixels++;
                    }
                }

                if (edgePixels > 40) {
                    horizontalCount++;
                    horizontalEdgePixels += edgePixels;
                }
            }

            // 邊緣偵測（垂直掃描）
            int verticalCount = 0;
            int verticalEdgePixels = 0;

            for (int x = 0; x < width; x++) {
                int edgePixels = 0;

                for (int y = 1; y < height; y++) {

                    int diff = Math.abs(gray[y][x] - gray[y - 1][x]);

                    // 調整垂直掃描的阈值（更嚴格以減少誤檢）
                    if (diff > 53) {
                        edgePixels++;
                    }
                }

                // 調整垂直掃描的邊界線判斷標準
                if (edgePixels > 45) {
                    verticalCount++;
                    verticalEdgePixels += edgePixels;
                }
            }

            // 精確計算鳳梨數量
            // 根據檢測邏輯：每個鳳梨應該產生多個邊界線
            // 水平掃描：檢測到的邊界線數 / 平均每個鳳梨的邊界線數
            // 垂直掃描：檢測到的邊界線數 / 平均每個鳳梨的邊界線數
            
            double horizontalPineapples = 0;
            double verticalPineapples = 0;
            
            if (horizontalCount > 0) {
                // 平均每個鳳梨的邊界線數約為 16-18 條
                horizontalPineapples = horizontalCount / 17.0;
            }
            
            if (verticalCount > 0) {
                // 垂直掃描調整係數（考慮更高的阈值，減少誤檢）
                verticalPineapples = verticalCount / 15.0;
            }
            
            // 結合兩種掃描的結果，取加權平均
            double finalCount = (horizontalPineapples + verticalPineapples) / 2.0;
            
            // 四舍五入到整數
            int pineappleCount = (int) Math.round(finalCount);

            System.out.println("====== 邊緣檢測結果 ======");
            System.out.println("水平掃描 - 邊界線數: " + horizontalCount);
            System.out.println("水平掃描 - 邊界像素數: " + horizontalEdgePixels);
            System.out.println("水平掃描 - 估算鳳梨數: " + String.format("%.2f", horizontalPineapples));
            System.out.println();
            System.out.println("垂直掃描 - 邊界線數: " + verticalCount);
            System.out.println("垂直掃描 - 邊界像素數: " + verticalEdgePixels);
            System.out.println("垂直掃描 - 估算鳳梨數: " + String.format("%.2f", verticalPineapples));
            System.out.println();
            System.out.println("====== 最終結果 ======");
            System.out.println("鳳梨數量平均值: " + String.format("%.2f", finalCount));
            System.out.println("鳳梨數量（四舍五入）: " + pineappleCount);

        } catch (IOException e) {
            System.out.println("Error loading image.");
        }
    }
}