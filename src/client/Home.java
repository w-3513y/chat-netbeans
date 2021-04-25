package client;

import common.Utils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Home extends JFrame{

    private ArrayList<String> opened_chats;
    private Map<String, ClientListener> connected_listeners;
    private ArrayList<String> connected_users;
    private String connection_info;
    private Socket connection;
    private ServerSocket server;
    private boolean running; 

    private JLabel jl_title;
    private JButton jb_get_connected, jb_start_talk;
    private JList jlist;
    private JScrollPane scroll;                        

    public Home(Socket connection, String connection_info)
    {
        super("Chat - Home");
        this.connection = connection;
        this.connection_info = connection_info;
        initComponents();
        configComponents();
        insertComponents();
        insertActions();
        start();
    }

    private void initComponents()
    {
        running = false;
        server = null;
        connected_listeners = new HashMap<String, ClientListener>();
        opened_chats = new ArrayList<String>();
        connected_users = new ArrayList<String>();
        jl_title = new JLabel("< Usuário: "+connection_info.split(":")[0]+">", SwingConstants.CENTER);
        jb_get_connected = new JButton("Atualizar Contatos");
        jb_start_talk = new JButton("Abrir Conversa");
        jlist = new JList();
        scroll = new JScrollPane(jlist);
    }

    private void configComponents()
    {
        this.setLayout(null);
        this.setMinimumSize(new Dimension(610, 490));
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Color.white);

        jl_title.setBounds(10, 10, 370, 40);
        jl_title.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        jb_get_connected.setBounds(400, 10, 180, 40);
        jb_get_connected.setFocusable(false);

        jb_start_talk.setBounds(10, 400, 575, 40);
        jb_start_talk.setFocusable(false);

        jlist.setBorder(BorderFactory.createTitledBorder("Usuários Online"));
        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scroll.setBounds(10, 60, 575, 335);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
    }

    private void insertComponents()
    {
        this.add(jl_title);
        this.add(jb_get_connected);
        this.add(jb_start_talk);
        this.add(scroll);

    }

    private void insertActions()
    {//7
        this.addWindowListener(new WindowListener()
        {            
            @Override
            public void windowOpened(WindowEvent e){               
            }
            @Override
            public void windowClosing(WindowEvent e){ 
                running = false;
                Utils.sendMessage(connection, "QUIT");
                System.out.println("> Conexão encerrada");
            }
            @Override
            public void windowClosed(WindowEvent e){                
            }
            @Override
            public void windowIconified(WindowEvent e){                
            }
            @Override
            public void windowDeiconified(WindowEvent e){                
            }
            @Override
            public void windowActivated(WindowEvent e){                
            }
            @Override
            public void windowDeactivated(WindowEvent e){                
            }                                           
        });
        jb_get_connected.addActionListener(event -> getConnectedUsers());
        jb_start_talk.addActionListener(event -> OpenChat());

    }

    private void start()
    {
        this.pack();
        this.setVisible(true);
        StartServer(this, Integer.parseInt(connection_info.split(":")[2]));

    }
    
    private void getConnectedUsers()
    {
        Utils.sendMessage(connection, "GET_CONNECTED_USERS");
        String response = Utils.receiveMessage(connection);
        jlist.removeAll();
        connected_users.clear();
        for (String info: response.split(";"))
        {
            if(!info.equals(connection_info))
            {
                connected_users.add(info);                
            }                          
        }
        jlist.setListData(connected_users.toArray());
                
    }
            
    public ArrayList<String> getOpened_chats() {
        return opened_chats;
    }

    public Map<String, ClientListener> getConnected_listeners() {
        return connected_listeners;
    }

    public String getConnection_info() {
        return connection_info;
    }
    
    private void OpenChat()
    {
        int index = jlist.getSelectedIndex();
        if(index != -1)
        {
            String connection_info = jlist.getSelectedValue().toString();
            String[] splited = connection_info.split(":");
            if(!opened_chats.contains(connection_info))
            {
                try 
                {
                    Socket connection = new Socket(splited[1], Integer.parseInt(splited[2]));
                    Utils.sendMessage(connection, "OPEN_CHAT;" + this.connection_info);
                    ClientListener cl = new ClientListener(this, connection);
                    cl.setChat(new Chat(this, connection, connection_info, this.connection_info.split(":")[0]));                    
                    cl.setChatOpen(true);
                    connected_listeners.put(connection_info, cl);
                    opened_chats.add(connection_info);
                    new Thread(cl).start();
                } 
                catch (IOException ex) 
                {
                    System.err.println("[Home:openChat] -> "+ ex.getMessage());
                }                
            }            
        }        
    }
    
    private void StartServer(Home home, int port)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                running = true;
                try
                {
                    server = new ServerSocket(port);
                    System.out.println("Servidor cliente iniciado na porta "+ port + "...");
                    while(running)
                    {
                        Socket connection = server.accept();
                        ClientListener cl = new ClientListener(home, connection);
                        new Thread(cl).start();
                    }                    
                }
                catch(IOException ex)
                {
                    System.err.println("[Home:startServer] -> "+ ex.getMessage());
                }
            }                        
        }.start();
        
    }    
    
  /* public static void main(String[] args)
    {
        Home home = new Home(null, "Dinho:127.0.0.1:3333");
    }   */
}
