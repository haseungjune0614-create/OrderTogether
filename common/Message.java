package common;

/**
 * 받은 메시지 한 줄을 해석한 결과.
 * 예) "JOIN|1|영희"  →  command = "JOIN", fields = ["1", "영희"]
 */
public class Message {
    public final String command;   // 맨 앞의 명령 (ORDER, JOIN, ERR ...)
    public final String[] fields;  // 명령 뒤의 필드들

    public Message(String command, String[] fields) {
        this.command = command;
        this.fields = fields;
    }

    /** i번째 필드를 돌려준다. 없으면 빈 문자열("")을 돌려줘서 오류가 나지 않게 한다. */
    public String field(int i) {
        if (i < fields.length) {
            return fields[i];
        }
        return "";
    }

    /** 필드 개수 */
    public int size() {
        return fields.length;
    }

    @Override
    public String toString() {
        return command + " " + java.util.Arrays.toString(fields);
    }
}