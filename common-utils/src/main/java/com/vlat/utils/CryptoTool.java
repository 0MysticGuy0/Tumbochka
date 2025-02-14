package com.vlat.utils;

import lombok.var;
import org.hashids.Hashids;

public class CryptoTool {
    private  final Hashids hashids;

    public CryptoTool(String salt) {
        var minHashLength = 10;
        this.hashids = new Hashids(salt, minHashLength);
    }

    public String hashOf(Long val){
        return hashids.encode(val);
    }

    public Long idOf(String val){
        long[] res = hashids.decode(val);
        if(res != null && res.length > 0){
            return res[0];
        }
        return null;
    }
}
