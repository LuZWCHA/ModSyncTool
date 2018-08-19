package com.software.beans;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 陆正威 on 2018/7/15.
 */
//unused
public final class ObjectPool<OBJECT extends ObjectPool.RecyclableObjectImp> {
    private OBJECT[] mTable;
    private AtomicInteger mOrderNumber;
    public static int RESET_NUM = 1000;

    public ObjectPool(OBJECT[] inputArray) {
        mOrderNumber = new AtomicInteger(0);
        mTable = inputArray;
        if (mTable == null) {
            throw new NullPointerException("The input array is null.");
        }
        int length = inputArray.length;
        if ((length & length - 1) != 0) {
            throw new RuntimeException("The length of input array is not 2^n.");
        }
    }

    public void recycle(OBJECT object) {
        object.Idle().set(true);
    }


    public OBJECT obtain() {
        int index = mOrderNumber.getAndIncrement();
        if (index > RESET_NUM) {
            mOrderNumber.compareAndSet(index, 0);
            if (index > RESET_NUM * 2) {
                mOrderNumber.set(0);
            }
        }

        int num = index & (mTable.length - 1);

        OBJECT target = mTable[num];

        if (target.Idle().compareAndSet(true, false)) {
            return target;
        } else {
            // 注意：此处可能会因为OBJECT回收不及时，而导致栈溢出。
            // 请增加retryTime参数，以及retryTime过多后的判断。
            // 具体思路请参考
            // https://github.com/SpinyTech/ModularizationArchitecture/blob/master/macore/src/main/java/com/spinytech/macore/router/RouterRequest.java
            // 中的obtain()及obtain(int retryTime);方法
            return obtain();
        }
    }

    public abstract static class RecyclableObject implements RecyclableObjectImp{
        private AtomicBoolean Idle = new AtomicBoolean(true);

        @Override
        public AtomicBoolean Idle() {
            return Idle;
        }
    }

    public interface RecyclableObjectImp{
        AtomicBoolean Idle();
    }
}
