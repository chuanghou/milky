package com.stellariver.milky.infrastructure.base.database;

/** {@link MilkyMapper} 乐观锁增强：受影响行数为 0 则抛 {@link LockConflictException}。 */
final class LockConflictExpect {

    private LockConflictExpect() {
    }

    static void assertAffected(String operation, int affectedRows) throws LockConflictException {
        if (affectedRows != 0) {
            return;
        }
        throw new LockConflictException(
                operation + ": affected rows = 0 (optimistic version mismatch, row missing, or not eligible)");
    }
}
