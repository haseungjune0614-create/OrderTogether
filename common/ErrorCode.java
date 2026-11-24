package common;

public enum ErrorCode {
    ALREADY_IN_ROOM,  // 이미 방에 있는데 또 들어가려 함
    NOT_IN_ROOM,      // 방에 없는데 주문 등을 하려 함
    NO_ROOM,          // 없는 방
    ROOM_FULL,        // 5명 꽉 참
    DUP_NICK,         // 같은 닉네임 있음
    INVALID_MENU,     // 없는 메뉴, 이상한 수량
    NOT_HOST,         // 방장이 아님
    CLOSED,           // 이미 마감됨
    INVALID_NAME,     // 이름이 비었거나 | , : 포함
    INVALID_TOKEN     // 재접속 토큰이 틀림
}