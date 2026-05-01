package com.stellariver.milky.infrastructure.base.database;

/**
 * Mapper 写操作在启用乐观锁（或「期望至少更新一行」）语义下，受影响行数为 0 时抛出。
 * <p>常见于 {@code @Version} 不匹配、记录不存在或不符合 WHERE 条件。</p>
 * <p>受检异常：调用 {@link MilkyBaseMapper} 的 {@code *WithOptimisticLock} 方法须捕获或在方法上 {@code throws}。</p>
 */
public class LockConflictException extends Exception {

    private static final long serialVersionUID = 1L;

    public LockConflictException(String message) {
        super(message);
    }

    public LockConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
