package frontEnd;

import java.io.IOException;

import client.Client;

public class Launchr {
    
    public static void main(String[] args) throws IOException {
        TerminalApp app = new TerminalApp(new Client());
        app.run(args);
    }
}
