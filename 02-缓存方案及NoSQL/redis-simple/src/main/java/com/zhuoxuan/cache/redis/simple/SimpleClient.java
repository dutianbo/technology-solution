package com.zhuoxuan.cache.redis.simple;

import org.junit.Test;

import com.zhuoxuan.common.entity.UserDO;

/**
 * 
 * <p>
 *  测试独立redis 客户端
 * </p>
 * 
 * @author 卓轩
 * @创建时间：2014年7月11日
 * @version： V1.0
 */
public class SimpleClient {
	
	@Test
	public void userCache(){
		
		//向缓存中保存对象
		UserDO zhuoxuan = new UserDO();
		zhuoxuan.setUserId(113445);
		zhuoxuan.setSex(1);
		zhuoxuan.setUname("张三");
		zhuoxuan.setUnick("zhangsan");
		zhuoxuan.setEmail("zhangsan@126.com");
		//调用方法处理
		boolean reusltCache = RedisClient.set("zhuoxuan", zhuoxuan);
		if (reusltCache) {
			System.out.println("向缓存中保存对象成功。");
		}else{
			System.out.println("向缓存中保存对象失败。");
		}
	}
	
	
	@Test
	public void getUserInfo(){
		
		UserDO zhuoxuan = RedisClient.get("zhuoxuan",UserDO.class);
		if(zhuoxuan != null){
			System.out.println("从缓存中获取的对象，" + zhuoxuan.getUname() + "@" + zhuoxuan.getEmail());
		}
		
	}
	
	

}
