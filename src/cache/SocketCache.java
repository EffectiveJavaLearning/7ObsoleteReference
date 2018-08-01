package cache;

import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 这个类用来说明缓存导致内存泄漏的第一种情况的解决方案——弱引用。
 * 假设我们需要在这个类中构建一个简单缓存以维护要缓存的对象
 *
 * 由于{@link WeakHashMap}的设计，“An entry in a WeakHashMap will automatically be removed when
 * its key is no longer in ordinary use.”如果我们明确地知道什么时候不会再用到某一缓存内的对象，
 * 而且还知道这个对象所对的键(KEY)，就可以使用Weak Preference代替缓存如下{@link #m}，
 * 那么我们可以在缓存之外手动取消该键的使用，这样就可以使它被自动回收。
 *
 * 但是，更多情况下什么时候不再使用某一元素是很难确定的，随着时间推移，缓存中的内容会变得越来越没价值，
 * 这种情况应该新开一个后台线程(Timer或者ScheduledThreadPoolExecutor),时不时清理掉没用的项，
 * 或者在给缓存添加新条目时顺便清理。
 *
 * 解决方案一：
 * {@link LinkedHashMap#removeEldestEntry(Map.Entry)}会在新元素被加入时，
 * 由{@link LinkedHashMap#afterNodeInsertion(boolean)}调用，默认返回false不令原有最老的元素被移除，
 * 我们可以通过重写这个方法，进而消除对过时对象的引用防止内存泄漏。
 *
 * *注：具体如何重写方式也有很多，这里removeEldestEntry其实是借鉴了LRU(最近最少使用)的思想，
 * 其他还有很多其他思路，比如FIFO(先进先出)，NRU(非最近使用)等，
 * 如果学《操作系统》会有讲到，感兴趣也可以搜一搜
 *
 * 解决方案二：
 * 这种常见的情况当然不会逃过JDK设计者大佬们的法眼，{@link WeakHashMap#expungeStaleEntries()}
 * 就是专门负责这个的，它去掉引用队列中所有失效的引用，并删除关联的映射，用引用队列代替周期性的对全部元素扫描，
 * 大多数Map操作会用到它。
 *
 * @author LightDance
 */
public class SocketCache extends LinkedHashMap {

    /**
     * 配合{@link SocketCache}的user类
     */
    private class User {private String username;}

    private SocketCache(){}

    private static final SocketCache INSTANCE = new SocketCache();

    private static final Map<Socket,User> m = new WeakHashMap<Socket,User>();

    public static SocketCache getInstance(){
        return INSTANCE;
    }

    public void setUser(Socket s, User u) {
        m.put(s, u);
    }
    public User getUser(Socket s) {
        return m.get(s);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        // 重写该方法以判断是否处理最老元素，
        // 比如 return size() > maxCapacity
        return super.removeEldestEntry(eldest);
    }
}