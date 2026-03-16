import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class WolfGaming {

    enum Role {
        WOLF("狼人"),
        VILLAGER("村民"),
        SEER("預言家"),
        WITCH("女巫"),
        HUNTER("獵人");

        private final String display;

        Role(String display) {
            this.display = display;
        }

        public String display() {
            return display;
        }
    }

    static class Player {
        private final int id;
        private final Role role;
        private boolean alive;

        Player(int id, Role role) {
            this.id = id;
            this.role = role;
            this.alive = true;
        }

        int getId() {
            return id;
        }

        Role getRole() {
            return role;
        }

        boolean isAlive() {
            return alive;
        }

        void kill() {
            this.alive = false;
        }

        String publicInfo() {
            return "玩家 " + id + " - " + (alive ? "存活" : "死亡");
        }
    }

    static class GameState {
        boolean witchHasAntidote = true;
        boolean witchHasPoison = true;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println("=== 歡迎來到桌遊狼人殺（終端機版）===");
        int numPlayers = readIntInRange(sc, "請輸入玩家人數（6~12）：", 6, 12);

        List<Role> rolePool = buildRolePool(numPlayers);
        Collections.shuffle(rolePool, rand);

        Player[] players = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            players[i] = new Player(i + 1, rolePool.get(i));
        }

        showRoleAssignment(sc, players);

        GameState state = new GameState();
        int round = 1;

        while (true) {
            System.out.println("\n============================");
            System.out.println("第 " + round + " 回合");
            System.out.println("============================");

            runNightPhase(sc, players, state);
            applyHunterSkillIfNeeded(sc, players, "夜晚");

            if (checkAndPrintWinner(players)) {
                break;
            }

            runDayPhase(sc, players);
            applyHunterSkillIfNeeded(sc, players, "白天");

            if (checkAndPrintWinner(players)) {
                break;
            }

            round++;
        }

        revealAllRoles(players);
        sc.close();
    }

    private static List<Role> buildRolePool(int n) {
        int wolfCount;
        if (n <= 7) {
            wolfCount = 2;
        } else if (n <= 10) {
            wolfCount = 3;
        } else {
            wolfCount = 4;
        }

        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < wolfCount; i++) {
            roles.add(Role.WOLF);
        }
        roles.add(Role.SEER);
        roles.add(Role.WITCH);
        if (n >= 8) {
            roles.add(Role.HUNTER);
        }

        while (roles.size() < n) {
            roles.add(Role.VILLAGER);
        }
        return roles;
    }

    private static void showRoleAssignment(Scanner sc, Player[] players) {
        System.out.println("\n=== 發身份階段 ===");
        for (Player p : players) {
            System.out.println("\n請玩家 " + p.getId() + " 查看身份，按 Enter 繼續...");
            sc.nextLine();
            System.out.println("你的身份是：" + p.getRole().display());
            System.out.println("請記住身份，按 Enter 清畫面交給下一位。");
            sc.nextLine();
            printBlankLines(25);
        }
    }

    private static void runNightPhase(Scanner sc, Player[] players, GameState state) {
        System.out.println("\n--- 夜晚開始 ---");

        Integer wolfTarget = wolvesAction(sc, players);
        seerAction(sc, players);
        NightResult result = witchAction(sc, players, state, wolfTarget);

        List<Integer> deadTonight = new ArrayList<>();

        if (result.finalWolfVictim != null) {
            deadTonight.add(result.finalWolfVictim);
        }
        if (result.poisonVictim != null && !deadTonight.contains(result.poisonVictim)) {
            deadTonight.add(result.poisonVictim);
        }

        if (deadTonight.isEmpty()) {
            System.out.println("昨晚是平安夜，無人死亡。");
        } else {
            for (int id : deadTonight) {
                Player p = players[id - 1];
                if (p.isAlive()) {
                    p.kill();
                }
            }
            System.out.print("昨晚死亡玩家：");
            for (int i = 0; i < deadTonight.size(); i++) {
                if (i > 0) {
                    System.out.print("、");
                }
                System.out.print("" + deadTonight.get(i));
            }
            System.out.println();
        }
    }

    private static Integer wolvesAction(Scanner sc, Player[] players) {
        if (countAliveByRole(players, Role.WOLF) == 0) {
            return null;
        }

        System.out.println("\n[狼人行動] 請狼人睜眼，選擇今晚刀口。可選存活且非狼玩家。");
        printAlivePlayers(players);

        while (true) {
            int target = readInt(sc, "狼人請輸入要擊殺的玩家編號：");
            if (!isAliveId(players, target)) {
                System.out.println("此玩家不存在或已死亡。請重試。");
                continue;
            }
            if (players[target - 1].getRole() == Role.WOLF) {
                System.out.println("狼人不能刀狼人。請重試。");
                continue;
            }
            return target;
        }
    }

    private static void seerAction(Scanner sc, Player[] players) {
        Player seer = findFirstAliveByRole(players, Role.SEER);
        if (seer == null) {
            return;
        }

        System.out.println("\n[預言家行動] 預言家請睜眼，選擇一位玩家查驗身份。");
        printAlivePlayers(players);

        while (true) {
            int target = readInt(sc, "預言家請輸入查驗目標編號：");
            if (!isAliveId(players, target)) {
                System.out.println("此玩家不存在或已死亡。請重試。");
                continue;
            }
            if (target == seer.getId()) {
                System.out.println("不能查驗自己。請重試。");
                continue;
            }
            Role role = players[target - 1].getRole();
            System.out.println("查驗結果：玩家 " + target + " 是 " + (role == Role.WOLF ? "狼人" : "好人陣營") + "。按 Enter 繼續。");
            sc.nextLine();
            printBlankLines(12);
            return;
        }
    }

    static class NightResult {
        Integer finalWolfVictim;
        Integer poisonVictim;
    }

    private static NightResult witchAction(Scanner sc, Player[] players, GameState state, Integer wolfTarget) {
        NightResult result = new NightResult();
        result.finalWolfVictim = wolfTarget;

        Player witch = findFirstAliveByRole(players, Role.WITCH);
        if (witch == null) {
            return result;
        }

        System.out.println("\n[女巫行動] 女巫請睜眼。");

        if (wolfTarget != null) {
            System.out.println("今晚刀口是玩家 " + wolfTarget + "。");
            if (state.witchHasAntidote) {
                String ans = readYesNo(sc, "是否使用解藥救人？(y/n)：");
                if (ans.equals("y")) {
                    result.finalWolfVictim = null;
                    state.witchHasAntidote = false;
                    System.out.println("你使用了解藥。");
                }
            } else {
                System.out.println("你已使用過解藥。");
            }
        } else {
            System.out.println("今晚狼人無法行動。\n");
        }

        if (state.witchHasPoison) {
            String ans = readYesNo(sc, "是否使用毒藥毒人？(y/n)：");
            if (ans.equals("y")) {
                printAlivePlayers(players);
                while (true) {
                    int target = readInt(sc, "請輸入要毒的玩家編號：");
                    if (!isAliveId(players, target)) {
                        System.out.println("此玩家不存在或已死亡。請重試。");
                        continue;
                    }
                    if (target == witch.getId()) {
                        System.out.println("不能毒自己。請重試。");
                        continue;
                    }
                    result.poisonVictim = target;
                    state.witchHasPoison = false;
                    System.out.println("你使用了毒藥。");
                    break;
                }
            }
        } else {
            System.out.println("你已使用過毒藥。");
        }

        printBlankLines(10);
        return result;
    }

    private static void runDayPhase(Scanner sc, Player[] players) {
        System.out.println("\n--- 白天開始 ---");
        printAlivePlayers(players);
        System.out.println("\n自由發言結束後，進入投票。每位存活玩家投 1 票。\n");

        Map<Integer, Integer> voteCount = new HashMap<>();
        List<Integer> alive = getAliveIds(players);

        for (int voterId : alive) {
            while (true) {
                int target = readInt(sc, "玩家 " + voterId + " 請投票給（玩家編號）：");
                if (!alive.contains(target)) {
                    System.out.println("只能投給存活玩家。請重試。");
                    continue;
                }
                voteCount.put(target, voteCount.getOrDefault(target, 0) + 1);
                break;
            }
        }

        int maxVote = 0;
        List<Integer> top = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : voteCount.entrySet()) {
            int id = e.getKey();
            int cnt = e.getValue();
            if (cnt > maxVote) {
                maxVote = cnt;
                top.clear();
                top.add(id);
            } else if (cnt == maxVote) {
                top.add(id);
            }
        }

        if (top.size() != 1) {
            System.out.println("平票，今天無人出局。");
            return;
        }

        int executed = top.get(0);
        players[executed - 1].kill();
        System.out.println("今天被公投出局的是：玩家 " + executed + "（身份：" + players[executed - 1].getRole().display() + "）");
    }

    private static void applyHunterSkillIfNeeded(Scanner sc, Player[] players, String phase) {
        List<Player> deadHunters = findDeadHunters(players);
        for (Player hunter : deadHunters) {
            System.out.println("\n[獵人技能] " + phase + "死亡的獵人（玩家 " + hunter.getId() + "）可開槍帶走一人。");
            printAlivePlayers(players);
            if (countAlive(players) == 0) {
                continue;
            }

            while (true) {
                int target = readInt(sc, "獵人請輸入要帶走的玩家編號（輸入 0 放棄）：");
                if (target == 0) {
                    System.out.println("獵人選擇不開槍。");
                    break;
                }
                if (!isAliveId(players, target)) {
                    System.out.println("此玩家不存在或已死亡。請重試。");
                    continue;
                }
                players[target - 1].kill();
                System.out.println("獵人帶走了玩家 " + target + "。\n");
                break;
            }
        }
    }

    private static List<Player> findDeadHunters(Player[] players) {
        List<Player> result = new ArrayList<>();
        for (Player p : players) {
            if (!p.isAlive() && p.getRole() == Role.HUNTER) {
                if (!processedDeadHunterIds.contains(p.getId())) {
                    processedDeadHunterIds.add(p.getId());
                    result.add(p);
                }
            }
        }
        return result;
    }

    private static final List<Integer> processedDeadHunterIds = new ArrayList<>();

    private static boolean checkAndPrintWinner(Player[] players) {
        int wolves = countAliveByRole(players, Role.WOLF);
        int good = countAlive(players) - wolves;

        if (wolves == 0) {
            System.out.println("\n🎉 好人陣營勝利！所有狼人已出局。");
            return true;
        }
        if (wolves >= good) {
            System.out.println("\n🐺 狼人陣營勝利！狼人數量已達到或超過好人。");
            return true;
        }
        return false;
    }

    private static void revealAllRoles(Player[] players) {
        System.out.println("\n=== 遊戲結束，身份揭曉 ===");
        for (Player p : players) {
            System.out.println("玩家 " + p.getId() + "：" + p.getRole().display() + "（" + (p.isAlive() ? "存活" : "死亡") + "）");
        }
    }

    private static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            int v = readInt(sc, prompt);
            if (v >= min && v <= max) {
                return v;
            }
            System.out.println("輸入範圍錯誤，請輸入 " + min + "~" + max + "。\n");
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("請輸入整數。\n");
            }
        }
    }

    private static String readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String ans = sc.nextLine().trim().toLowerCase();
            if (ans.equals("y") || ans.equals("n")) {
                return ans;
            }
            System.out.println("請輸入 y 或 n。\n");
        }
    }

    private static int countAlive(Player[] players) {
        int c = 0;
        for (Player p : players) {
            if (p.isAlive()) {
                c++;
            }
        }
        return c;
    }

    private static int countAliveByRole(Player[] players, Role role) {
        int c = 0;
        for (Player p : players) {
            if (p.isAlive() && p.getRole() == role) {
                c++;
            }
        }
        return c;
    }

    private static Player findFirstAliveByRole(Player[] players, Role role) {
        for (Player p : players) {
            if (p.isAlive() && p.getRole() == role) {
                return p;
            }
        }
        return null;
    }

    private static boolean isAliveId(Player[] players, int id) {
        return id >= 1 && id <= players.length && players[id - 1].isAlive();
    }

    private static List<Integer> getAliveIds(Player[] players) {
        List<Integer> ids = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive()) {
                ids.add(p.getId());
            }
        }
        return ids;
    }

    private static void printAlivePlayers(Player[] players) {
        System.out.println("目前玩家狀態：");
        for (Player p : players) {
            System.out.println(p.publicInfo());
        }
    }

    private static void printBlankLines(int lines) {
        for (int i = 0; i < lines; i++) {
            System.out.println();
        }
    }
}