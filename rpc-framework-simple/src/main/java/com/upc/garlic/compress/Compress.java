package com.upc.garlic.compress;

import com.upc.garlic.extension.SPI;

@SPI
public interface Compress {
    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);

}
