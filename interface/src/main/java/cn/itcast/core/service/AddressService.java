package cn.itcast.core.service;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddressService {
    //查询呢用户的所有收货地址
    List<Address> findListByLoginUser(String username);
}
