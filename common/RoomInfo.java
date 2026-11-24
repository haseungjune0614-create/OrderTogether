package common;

/**
 * ROOM_LIST 의 방 하나.
 * 예) "1:치킨방:3/5:OPEN"  →  id=1, name="치킨방", users=3, max=5, closed=false
 */
public class RoomInfo {
    public final int id;
    public final String name;
    public final int users;    // 현재 인원
    public final int max;      // 최대 인원
    public final boolean closed;

    public RoomInfo(int id, String name, int users, int max, boolean closed) {
        this.id = id;
        this.name = name;
        this.users = users;
        this.max = max;
        this.closed = closed;
    }

    @Override
    public String toString() {
        return id + "번 " + name + " (" + users + "/" + max + ", " + (closed ? "CLOSED" : "OPEN") + ")";
    }
}