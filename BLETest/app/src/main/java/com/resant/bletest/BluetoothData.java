package com.resant.bletest;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BluetoothData {
    //  Класс для хранения получаемого значения
    private final byte[] result; //  Получаемое значение

    //  Константы, необходимые для преобразования чисел
    public static final int INT32_T = 0;
    public static final int UINT8_T = 1;

    public BluetoothData(byte[] result)
    {
        this.result=result;
    }

    //  Получение байтов в чистом виде
    public byte[] getResult() {
        return result.clone();
    }

    //  Превращение байтов в числа
    public int[] getData(int[] types)
    {

        int[] res = new int[types.length]; //  Массив с результатом

        ByteBuffer buffer = ByteBuffer.wrap(result); //  Буффер байтов
        buffer.order(ByteOrder.LITTLE_ENDIAN); //  Порядок

        for(int i = 0; i<res.length;i++)
        {
            //  Преобразование в числа
            int type = types[i];
            switch (type)
            {
                case INT32_T:
                    Log.println(Log.DEBUG,"dataforme",""+buffer.remaining());
                    res[i] = buffer.getInt();
                    break;

                case UINT8_T:
                    res[i] = buffer.get()&0xFF;
                    break;
            }
        }


        return res;
    }

}
