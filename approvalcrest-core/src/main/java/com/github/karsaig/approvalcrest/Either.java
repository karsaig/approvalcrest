package com.github.karsaig.approvalcrest;

import java.util.function.Consumer;

public class Either<L,R> {

    private final L left;
    private final R right;
    private boolean isLeft;

    private Either(L left, R right, boolean isLeft) {
        this.left = left;
        this.right = right;
        this.isLeft = isLeft;
    }

    public static <L,R> Either<L,R> left(L leftValue){
        return new Either<>(leftValue, null, true);
    }

    public static <L,R> Either<L,R> right(R rightValue){
        return new Either<>(null, rightValue, false);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return !isLeft;
    }

    public void fold(Consumer<L> ifLeft, Consumer<R> ifRight) {
        if(isLeft) {
            ifLeft.accept(left);
        } else {
            ifRight.accept(right);}
    }
}
