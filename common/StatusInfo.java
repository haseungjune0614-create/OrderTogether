package common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * STATUS(실시간 현황) · FINAL(최종 주문표)의 내용.
 * 예) "STATUS|A:1:2,A:3:1,B:2:1|1:2,2:1,3:1|57000"
 *     byUser = {A={1=2, 3=1}, B={2=1}}   사람별 주문
 *     counts = {1=2, 2=1, 3=1}           메뉴별 수량
 *     total  = 57000                     총금액
 */
public class StatusInfo {
    public final Map<String, Map<Integer, Integer>> byUser;
    public final Map<Integer, Integer> counts;
    public final int total;

    public StatusInfo(Map<String, Map<Integer, Integer>> byUser, Map<Integer, Integer> counts, int total) {
        this.byUser = byUser;
        this.counts = counts;
        this.total = total;
    }

    /** 비어 있는 현황 (모든 주문이 초기화된 상태) */
    public static StatusInfo empty() {
        return new StatusInfo(new LinkedHashMap<>(), new LinkedHashMap<>(), 0);
    }

    @Override
    public String toString() {
        return "사람별 " + byUser + " / 메뉴별 " + counts + " / 총 " + total + "원";
    }
}