public  class WolfGaming{
    static class player{    //設定玩家資料的類別
        private int id;     //玩家ID
        private String rules;   //玩家規則
        private boolean alive;  //玩家是否存活

        public Player(){    //建構子，初始化玩家資料
            this.id = id;
            this.rules = rules;
            this.alive = true;
        }

        public int getId(){     //取得玩家ID
            return id;
        }

        public String getRules(){   //取得玩家規則
            return rules;
        }

        public boolean isAlive(){   //檢查玩家是否存活
            return alive;
        }

        public void death(){    //玩家死亡，將alive設為false，表示玩家不再存活
            alive = false;
        }

        public String getPublicInfo(){  //取得玩家公開資訊，顯示玩家ID和存活狀態
            if (alive){
                return "Player " + id + ": " + "[Alive]";   //如果玩家存活，顯示玩家ID和存活狀態為Alive
            }
            else{
                return "Player " + id + ": " + "[Dead]";    //如果玩家死亡，顯示玩家ID和存活狀態為Dead
            }
        }

        public static void main(String[] args){     //主方法，程式入口點
            Scanner sc = new Scanner(System.in);    //創建Scanner對象，用於讀取用戶輸入，鍵盤輸入
            Random rand = new Random();     //創建Random對象，用於生成隨機數，模擬遊戲過程中的隨機事件

            System.out.println("Welcome to Wolf Gaming!");    //歡迎訊息
            System.out.println("Enter the number of players: ");   //玩家人數輸入

            int numPlayers = sc.nextInt();
            sc.nextLine();    //清除換行符

            while (numPlayers < 4 || numPlayers > 10){
                System.out.println("Invalid number of players. Please enter a number between 4 and 10: ");   //玩家人數規定提示
                numPlayers = sc.nextInt();
                sc.nextLine();    //清除換行符
            }

            Player[] players = new Player[numPlayers];    //創建玩家陣列，根據輸入的玩家人數初始化
            int wolfIndex = rand.nextInt(numPlayers);    //隨機選擇一個玩家作為狼人

            for (int i = 0; i < numPlayers; i++){   //初始化玩家陣列，根據隨機選擇狼人
                if (i == wolfIndex){
                    players[i] = new Player(i + 1, rules = "Wolf");    
                }
                else{
                    players[i] = new Player(i + 1, rules = "Villager");
                }
            }

            System.out.println();
            System.out.println("Role Assignment Started!");    //規則分配開始提示
            System.out.println("Each players take turn to role!");

            for(int i = 0; i < numPlayers; i++){
                System.out.println();    //換行
                System.out.println("Player " + players[i] + 1 + "Please enter ");
                sc.nextLine();
                System.out.println("Your role is: " + players[i].getRules());    //顯示玩家的角色
                System.out.println("Memorize your role!");    //提示玩家記住自己的角色
                sc.nextLine();    

                for(int line = 0; line < 30; line++){
                    System.out.println();    //換行，模擬清除效果，讓其他玩家無法看到角色資訊
                }
            }

            boolean gameOver = false;    //遊戲結束標誌
            int rounds = 1;    //回合數

            while(!gameOver){
                System.out.println("Round " + rounds);    //顯示當前回合數
                sc.nextLine();    //等待玩家按下Enter鍵繼續

                System.out.println("Night falls. Wolf wakes up");    //夜晚階段提示
                int aliveWolf = findAliveWolf(players);    //尋找存活的狼人

                if(aliveWolf != -1){
                    System.out.println("Wolf is your turn to choose a victim.");    //狼人選擇受害者提示
                    printAlivePlayers(players);    //顯示存活的玩家列表

                    int target = -1;

                    while(true){
                        System.out.println("Choose a player to kill ");

                        if(sc.hasNext()){
                            targetID = sc.nextInt();
                            sc.nextLine();    //清除輸入緩衝區

                            if(isValidTarget(targetID, players[aliveWolf].getId(), players)){
                                break;
                            }
                            else{
                                System.out.println("Invalid target. Please choose a valid player ID.");    //無效目標提示
                                sc.nextLine();    //清除輸入緩衝區
                            }
                        }
                    }
                    players[targetID - 1].death();    //將選擇的玩家設為死亡狀態
                    System.out.println("Night result: Player " + targetID + " has been killed by the Wolf.");    //提示玩家被狼人殺死 
                }
                else{
                    System.out.println("No alive Wolf found. Skipping night phase.");    //如果沒有存活的狼人，跳過夜晚階段提示
                }

                if(checkKillVillagerwin(players)){
                    System.out.println("Villagers win! All wolves have been killed.");    //村民勝利提示
                    gameOver = true;    //設置遊戲結束標誌為true
                }
                else if(checkKillWolfwin(players)){
                    System.out.println();
                    System.out.println("Wolf wins! All villagers have been killed.");    //狼人勝利提示
                    gameOver = true;    //設置遊戲結束標誌為true
                }
                if(gameOver){
                    break;    //跳出遊戲循環
                }

                int voteID = -1;    //投票階段，玩家投票選擇要處決的玩家

                public static int findAliveWolf(Player[] players){    //打印存活玩家列表
                    for (int i = 0; i < players.length; i++){
                        if(players[i].isAlive() && players[i].getRules().equals("Wolf")){
                            return i;
                        }
                    }
                    return -1;
                }

                public static void printAlivePlayers(Player[] players){
                    for(int i = 0; i < players.length; i++){
                        System.out.println(players[i].getPublicInfo());    //顯示每個玩家的公開資訊，包括ID和存活狀態
                    }
                }
            }
        }
    }
}