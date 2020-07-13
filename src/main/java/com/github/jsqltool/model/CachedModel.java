package com.github.jsqltool.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jsqltool.entity.ConnectionInfo;

/**
 * 缓存类的Model
 * @author yzh
 *
 * @date 2020年7月2日
 */
public class CachedModel implements IModel {

	private final IModel target;
	private final Map<String, CacheUserConnectionInfo> caches = new ConcurrentHashMap<>();

	private CachedModel(IModel target) {
		this.target = target;
	}

	public static IModel builder(IModel target) {
		return new CachedModel(target);
	}

	@Override
	public List<String> listConnection(String user) {
		CacheUserConnectionInfo cacheInfo = tryGetCacheUserConnectionInfo(user);
		return cacheInfo != null ? cacheInfo.listNames() : Collections.emptyList();
	}

	private void updateUserInfo(String user) {
		CacheUserConnectionInfo cacheInfo = new CacheUserConnectionInfo();
		List<ConnectionInfo> listConnectionInfo = target.listConnectionInfo(user);
		if (listConnectionInfo != null && !listConnectionInfo.isEmpty()) {
			for (ConnectionInfo info : listConnectionInfo)
				cacheInfo.add(info);
		}
		caches.put(user, cacheInfo);
	}

	@Override
	public List<ConnectionInfo> listConnectionInfo(String user) {
		CacheUserConnectionInfo cacheInfo = tryGetCacheUserConnectionInfo(user);
		return cacheInfo.listConnectionInfo();
	}

	/**
	 *  尝试获取CacheUserConnectionInfo，如果没有尝试从target中获取一次。
	* @author yzh
	* @date 2020年7月2日
	* @Description: 
	*  @param user
	*  @return    参数
	* @return CacheUserConnectionInfo    返回类型
	 */
	private CacheUserConnectionInfo tryGetCacheUserConnectionInfo(String user) {
		CacheUserConnectionInfo cacheInfo = caches.get(user);
		if (cacheInfo == null || cacheInfo.isEmpty()) {
			updateUserInfo(user);
			cacheInfo = caches.get(user);
		}
		return cacheInfo;
	}

	@Override
	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		CacheUserConnectionInfo infos = tryGetCacheUserConnectionInfo(user);
		if (infos != null && !infos.isEmpty())
			return infos.getConnectionInfo(connectionName);
		return null;
	}

	@Override
	public boolean save(String user, String oldConnectionName, ConnectionInfo info) {
		boolean save = target.save(user, oldConnectionName, info);
		updateUserInfo(user);
		return save;
	}

	@Override
	public boolean delete(String user, String connectionName) {
		boolean delete = target.delete(user, connectionName);
		updateUserInfo(user);
		return delete;
	}

	private static class CacheUserConnectionInfo {
		private ConcurrentHashMap<String, ConnectionInfo> maps = new ConcurrentHashMap<>();

		public void add(ConnectionInfo connectionInfo) {
			maps.put(connectionInfo.getName(), connectionInfo);
		}

		public boolean isEmpty() {
			return maps.isEmpty();
		}

		public List<String> listNames() {
			return new ArrayList<String>(maps.keySet());
		}

		public List<ConnectionInfo> listConnectionInfo() {
			return new ArrayList<>(maps.values());
		}

		public ConnectionInfo getConnectionInfo(String name) {
			return maps.get(name);
		}

	}

}
