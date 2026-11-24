package common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 공동 주문 취합 시스템의 통신 규약을 코드로 옮긴 클래스.
 * 서버와 클라이언트가 모두 이 클래스로 메시지를 해석하고 만든다.
 *
 * 규칙: 한 줄 = 메시지 하나, 형식은 명령|필드1|필드2|...
 *       여러 항목은 쉼표(,), 항목 안의 값은 콜론(:)으로 구분
 */
public class Protocol {

    // ===== 클라이언트 → 서버 명령 =====
    public static final String ROOMS = "ROOMS";
    public static final String CREATE = "CREATE";
    public static final String JOIN = "JOIN";
    public static final String ORDER = "ORDER";
    public static final String CANCEL = "CANCEL";
    public static final String CLEAR = "CLEAR";
    public static final String CLOSE = "CLOSE";
    public static final String LEAVE = "LEAVE";
    public static final String RECONNECT = "RECONNECT";
    public static final String PONG = "PONG";

    // ===== 서버 → 클라이언트 명령 =====
    public static final String OK = "OK";
    public static final String ERR = "ERR";
    public static final String ROOM_LIST = "ROOM_LIST";
    public static final String MENU = "MENU";
    public static final String STATUS = "STATUS";
    public static final String FINAL = "FINAL";
    public static final String ROOM_END = "ROOM_END";
    public static final String PING = "PING";

    // ===== 1. 해석하기 =====

    /**
     * 받은 한 줄을 명령과 필드로 나눈다.
     * 예) "ORDER|1:2,3:1"  →  command = "ORDER", fields = ["1:2,3:1"]
     *
     * split("\\|", -1) 의 -1 이 중요하다.
     * 이게 없으면 "STATUS|||0" 처럼 빈 칸이 있는 메시지에서 빈 필드가 사라져 개수가 틀어진다.
     */
    public static Message parse(String line) {
        if (line == null) {
            return new Message("", new String[0]);
        }
        // 윈도우에서 줄 끝에 붙을 수 있는 \r 제거
        if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
        }
        String[] parts = line.split("\\|", -1);
        String command = parts[0];
        String[] fields = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            fields[i - 1] = parts[i];
        }
        return new Message(command, fields);
    }

    // ===== 2. 만들기 =====

    /**
     * 명령과 필드를 | 로 이어 붙인다. (줄바꿈은 보낼 때 println 이 붙인다)
     * 예) build("JOIN", "1", "영희")  →  "JOIN|1|영희"
     */
    public static String build(String command, String... fields) {
        StringBuilder sb = new StringBuilder(command);
        for (String f : fields) {
            sb.append('|').append(f);
        }
        return sb.toString();
    }

    /** 성공 응답.  예) ok("CREATE", "1", "tk_a1")  →  "OK|CREATE|1|tk_a1" */
    public static String ok(String command, String... extra) {
        String[] fields = new String[extra.length + 1];
        fields[0] = command;
        for (int i = 0; i < extra.length; i++) {
            fields[i + 1] = extra[i];
        }
        return build(OK, fields);
    }

    /** 실패 응답.  예) error(ErrorCode.ROOM_FULL)  →  "ERR|ROOM_FULL" */
    public static String error(ErrorCode code) {
        return build(ERR, code.name());
    }

    /**
     * ERR 메시지에서 에러 코드를 꺼낸다. (클라이언트용)
     * 예) parse("ERR|ROOM_FULL") 를 넣으면 ErrorCode.ROOM_FULL
     * 모르는 코드면 null
     */
    public static ErrorCode errorCodeOf(Message m) {
        try {
            return ErrorCode.valueOf(m.field(0));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ===== 3. 주문 문자열 =====

    /**
     * 주문 문자열을 메뉴ID → 수량 으로 바꾼다.
     * 예) "1:2,3:1"  →  {1=2, 3=1}
     * 빈 문자열이면 빈 주문을 돌려준다.
     * 형식이 틀렸거나 수량이 0 이하면 null → 서버는 ERR|INVALID_MENU 로 응답하면 된다.
     * (메뉴 ID가 실제 메뉴판에 있는지는 메뉴판을 가진 서버가 확인)
     */
    public static Map<Integer, Integer> parseOrder(String text) {
        Map<Integer, Integer> order = new LinkedHashMap<>();
        if (text == null || text.isEmpty()) {
            return order;
        }
        for (String item : text.split(",", -1)) {
            String[] kv = item.split(":", -1);
            if (kv.length != 2) {
                return null;
            }
            try {
                int menuId = Integer.parseInt(kv[0].trim());
                int qty = Integer.parseInt(kv[1].trim());
                if (qty <= 0 || order.containsKey(menuId)) {
                    return null;
                }
                order.put(menuId, qty);
            } catch (NumberFormatException e) {
                return null;   // 숫자가 아님
            }
        }
        return order;
    }

    /** 메뉴ID → 수량 을 주문 문자열로 바꾼다.  예) {1=2, 3=1}  →  "1:2,3:1" */
    public static String formatOrder(Map<Integer, Integer> order) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : order.entrySet()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        return sb.toString();
    }

    // ===== 4. 검사하기 =====

    /**
     * 닉네임·방이름 검사. 비어 있거나 | , : 가 들어 있으면 false
     * → 서버는 ERR|INVALID_NAME 로 응답하면 된다.
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return !(name.contains("|") || name.contains(",") || name.contains(":"));
    }

    // ===== 직접 실행해서 확인해 보는 용도 =====
    public static void main(String[] args) {
        Message m = parse("ORDER|1:2,3:1");
        System.out.println("해석: " + m);
        System.out.println("주문: " + parseOrder(m.field(0)));

        System.out.println("빈칸 유지: " + parse("STATUS|||0").size() + "개 (3개여야 정상)");

        System.out.println(build(JOIN, "1", "영희"));
        System.out.println(ok(CREATE, "1", "tk_a1"));
        System.out.println(error(ErrorCode.ROOM_FULL));
        System.out.println("에러 코드: " + errorCodeOf(parse("ERR|ROOM_FULL")));

        System.out.println("틀린 주문(수량 0): " + parseOrder("1:0"));
        System.out.println("틀린 주문(글자): " + parseOrder("a:2"));
        System.out.println("주문 → 글자: " + formatOrder(parseOrder("1:2,3:1")));

        System.out.println("이름 '철수': " + isValidName("철수"));
        System.out.println("이름 '철|수': " + isValidName("철|수"));
        System.out.println("이름 '': " + isValidName(""));
    }
}