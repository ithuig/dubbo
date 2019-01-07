package cn.itcast.core.service;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressDao addressDao;
    @Override
    public List<Address> findListByLoginUser(String username) {
        AddressQuery addressQuery = new AddressQuery();
        AddressQuery.Criteria criteria = addressQuery.createCriteria();
        criteria.andUserIdEqualTo(username);
        List<Address> addresses = addressDao.selectByExample(addressQuery);
        return addresses;
    }
}
