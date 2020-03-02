package gcom.GroupManagement;

public class HostInfo {

	public int port = 2000;
	public String host = "localhost";

	public HostInfo(String host, int port) {
		this.port = port;
		this.host = host;
	}

	public String getPortString() {
		return String.valueOf(port);
	}
	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}
}
