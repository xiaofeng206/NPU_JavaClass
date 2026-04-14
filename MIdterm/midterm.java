import java.util.Arrays;

public class midterm {
	public static void main(String[] args) {
        //1.計算分數平均值
        double average = 0.0;
		int[] scores = {70, 80, 90};

        average = (scores[0] + scores[1] + scores[2]) / 3.0;
        System.out.println("Answer1 The average is: " + average);
        System.out.println();

        //2.找出最大值
        int[] findMax = {1,4,7,10,13,99,101};

        int max = findMax[0];
        for (int i = 1; i < findMax.length; i++) {
            if (findMax[i] > max) {
                max = findMax[i];
            }
        }

        System.out.println("Answer2 The maximum value is: " + max);
        System.out.println();

        //3.將分數加5分
        addBonus(scores);

        System.out.println("Answer3 Scores after +5: " + Arrays.toString(scores));
        System.out.println();

        //4.建立 Student Tom
        Student tom = new Student("Tom", 85);

        System.out.println("Answer4 " + tom.name + ": " + tom.score);
        System.out.println();

        //5.建立 Student 陣列並套用 curve
        Student[] students = {
            new Student("Amy", 55),
            new Student("Bob", 60),
            new Student("Cindy", 40)
        };

        System.out.println("Answer5 Before:");
        for (int i = 0; i < students.length; i++) {
            System.out.println(students[i].name + ": " + students[i].score);
        }

        for (int i = 0; i < students.length; i++) {
            curve(students[i]);
        }

        System.out.println("Answer5 After:");
        for (int i = 0; i < students.length; i++) {
            System.out.println(students[i].name + ": " + students[i].score);
        }
        System.out.println();

        //6.顯示全部分數
        int[] passScores = {45, 59, 60, 61, 75, 99};
        int countPass = 0;

        for (int i = 0; i < passScores.length; i++) {
            System.out.print(passScores[i] + " ");
            if (passScores[i] >= 60) {
                countPass++;
            }
        }

        System.out.println();
        System.out.println("Answer6 Number of passing: " + countPass);
        System.out.println();

        //7.計算分數總和
        int[] arr7 = {10, 20, 30, 40, 50, 60, 70, 80, 90};
        int total = sum(arr7);

        System.out.println("Answer7 The sum is: " + total);
        System.out.println();

        //8.建立 Student 物件陣列並列印姓名與分數
        Student[] students8 = {
            new Student("Ethan", 75),
            new Student("Amy", 88),
            new Student("Mack", 99)
        };

        System.out.println("Answer8 Student names and scores:");
        for (int i = 0; i < students8.length; i++) {
            System.out.println((i + 1) + "." + students8[i].name + " scores:" + students8[i].score);
        }
        System.out.println();

        //9.更新學生分數
        Student ethan = new Student("Ethan", 59);

        System.out.println("Answer9 Before update:");
        System.out.println("Ethan scores:" + ethan.score);
        updateScore(ethan, 65);
        System.out.println("Answer9 After update:");
        System.out.println("Ethan scores:" + ethan.score);
        System.out.println();

        //10.找出最小值
        int[] findMin = {1,4,7,10,13,99,101};
        int minIndex = 0;

        for (int i = 1; i < findMin.length; i++) {
            if (findMin[i] < findMin[minIndex]) {
                minIndex = i;
            }
        }

        System.out.println("Answer10");
        System.out.println("The minimum index is: " + minIndex);
        System.out.println("The minimum value is: " + findMin[minIndex]);
    }

//=========================================================================================
    //第3題的加5分
    public static void addBonus(int[] scores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += 5;
        }
	}

    //第5題如果未達60分就加10分
    public static void curve(Student s) {
        if (s.score < 60) {
            s.score += 10;
        }
    }

    //第7題計算分數總和
    public static int sum(int[] arr7) {
        int total = 0;
        
        for (int i = 0; i < arr7.length; i++) {
            total += arr7[i];
        }
        return total;
    }

    public static void updateScore(Student s, int newScore) {
        s.score = newScore;
    }
}

//學生的類別
class Student {
    String name;
    int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
