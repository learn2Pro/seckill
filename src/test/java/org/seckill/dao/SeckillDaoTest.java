package org.seckill.dao;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.common.KillCacheTemplate;
import org.seckill.constant.CacheConstant;
import org.seckill.entity.Seckill;
import org.seckill.util.SpringContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 配置spring和Junit整合，junit启动时加载IOC容器
 * @author baironglin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
//spring配置文件位置
@ContextConfiguration({"classpath:spring-config.xml"})
public class SeckillDaoTest {

	//注入Dao实现类依赖
	@Resource
	private SeckillDao seckillDao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testReduceNumber() {
		Date killTime = new Date();
		int updateCount = seckillDao.reduceNumber(1000L, killTime);
		System.out.println(updateCount);
	}

//    @Test
//    public void testJimdb(){
//        SpringContextUtils.init();
//        KillCacheTemplate.set("123", CacheConstant.Expiration.ONE_MINUTE,"abc");
//        System.out.println(KillCacheTemplate.get("123",String.class));
//    }

	@Test
	public void testQueryById() {
		long id = 1000;
		Seckill seckill = seckillDao.queryById(id);
		System.out.println(seckill);
	}

	@Test
	public void testQueryAll() {
		List<Seckill> res = seckillDao.queryAll(0, 100);
		for(Seckill s : res) {
			System.out.println(s);
		}
	}

    @Test
    public void addSeckill() {
       Seckill seckill=new Seckill();
        seckill.setName("dsadsa");
        seckill.setNumber(100);
        seckill.setCreateTime(new Date());
        seckillDao.addBySeckill(seckill);
    }

    @Test
    public void updateSeckill() {
        Seckill seckill=new Seckill();
        seckill.setSeckillId(1014);
        seckill.setName("dsadsa");
        seckill.setNumber(1000);
        seckillDao.updateBySecKill(seckill);
    }

}
