package net.vinote.smart.socket.protocol.p2p.client;

import java.util.Properties;
import java.util.logging.Level;

import net.vinote.smart.socket.lang.QuicklyConfig;
import net.vinote.smart.socket.lang.StringUtils;
import net.vinote.smart.socket.logger.RunLogger;
import net.vinote.smart.socket.protocol.P2PProtocolFactory;
import net.vinote.smart.socket.protocol.p2p.message.HeartMessageResp;
import net.vinote.smart.socket.protocol.p2p.message.LoginAuthReq;
import net.vinote.smart.socket.protocol.p2p.message.LoginAuthResp;
import net.vinote.smart.socket.protocol.p2p.message.P2pServiceMessageFactory;
import net.vinote.smart.socket.protocol.p2p.message.RemoteInterfaceMessageResp;
import net.vinote.smart.socket.service.factory.ServiceMessageFactory;
import net.vinote.smart.socket.service.filter.SmartFilter;
import net.vinote.smart.socket.service.filter.impl.FlowControlFilter;
import net.vinote.smart.socket.transport.nio.NioQuickClient;

public class P2PMultiClient {
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 10; i++) {
			new Thread() {

				@Override
				public void run() {

					QuicklyConfig config = new QuicklyConfig(false);
					config.setProtocolFactory(new P2PProtocolFactory());
					P2PClientMessageProcessor processor = new P2PClientMessageProcessor();
					config.setProcessor(processor);
					config.setFilters(new SmartFilter[] { new FlowControlFilter() });
					config.setHost("127.0.0.1");
					config.setTimeout(1000);

					Properties properties = new Properties();
					properties.put(HeartMessageResp.class.getName(), "");
					properties.put(RemoteInterfaceMessageResp.class.getName(), "");
					properties.put(LoginAuthResp.class.getName(), "");
					ServiceMessageFactory messageFactory = new P2pServiceMessageFactory();
					try {
						messageFactory.loadFromProperties(properties);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					config.setServiceMessageFactory(messageFactory);

					NioQuickClient client = new NioQuickClient(config);
					client.start();

					long num = Long.MAX_VALUE;
					long start = System.currentTimeMillis();
					while (num-- > 0) {
						LoginAuthReq loginReq = new LoginAuthReq(processor.getSession().getAttribute(
							StringUtils.SECRET_KEY, byte[].class));
						loginReq.setUsername("zjw");
						loginReq.setPassword("aa");
						LoginAuthResp loginResp;
						try {
							loginResp = (LoginAuthResp) processor.getSession().sendWithResponse(loginReq);
							// RunLogger.getLogger().log(Level.FINE,
							// StringUtils.toHexString(loginResp.getData()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					RunLogger.getLogger().log(Level.FINE, "安全消息结束" + (System.currentTimeMillis() - start));
					client.shutdown();
				}

			}.start();
			Thread.sleep(100);
		}

	}
}
