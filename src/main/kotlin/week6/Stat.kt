package week6

/**
 * Ready 는 딱 생성됬을때 상태
 * Mark 는 사용자 요구 사항 마킹 (Confirm 되지 않으면 의미 없음)
 * Confirm 사용자의 요구 사항 마킹 수용
 */
enum class Stat {
    READY, MARK, CONFIRM
}