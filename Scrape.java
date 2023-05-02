package Scrape;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scrape extends JFrame{
    //components
    JPanel mainPanel = new JPanel();
    JTextField tfURL = new JTextField();
    //JTextField tfREGEX = new JTextField(20);
    String[] REGEXOPTIONS = {
                     "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",  /*emails */
                     "[0-9]{3}-[0-9]{3}-[0-9]{4}", /*phone numbers */
                     "<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>(.*?)</\\1>", // html elements
                     "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b" // IPv4 addresses
    };
    JComboBox<String> cbREGEX = new JComboBox<String>(REGEXOPTIONS);
    JButton btClick = new JButton("Click to scrape!");
    JTable table;
    DefaultTableModel tm;
    JButton bt;
    JButton btSaveToFile;

    //methods
    public void GameOn(ActionEvent e) throws Exception
    {
        URL url = new URL(tfURL.getText());
        URLConnection con = url.openConnection() ;
        InputStream is = con.getInputStream();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(is)))
        {
            String line = null;
            while((line = br.readLine()) !=null )
            {
                Matcher m = Pattern.compile(cbREGEX.getSelectedItem().toString()).matcher(line);
                while(m.find())
                {
                    tm.insertRow(table.getRowCount(), new Object[]{table.getRowCount()+1, m.group()});
                }
            }
        }
    }

    public void SaveTableToFile(ActionEvent e) throws IOException {
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save results to file");

        int userSelection = fileChooser.showSaveDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            FileWriter fileWriter = new FileWriter(fileToSave);
            fileWriter.write("The results from: \n");
            fileWriter.write("\t URL: " + tfURL.getText() + " \n");
            fileWriter.write("\t REGEX: " + cbREGEX.getSelectedItem().toString() + " \n");
            for(Vector line : tm.getDataVector()){
                for(Object col : line)
                    fileWriter.write(col.toString() + " ");
                fileWriter.write( "\n");
            }
            fileWriter.close();
        }
        parentFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    
    public void resetResults() {
        tm.setRowCount(0); // remove all rows from the table model
    }
    
    
    //ctor
    public Scrape()  {
        mainPanel.setLayout(new BorderLayout());

        /////////////////// NORTH
        mainPanel.add(tfURL, BorderLayout.NORTH);

        /////////////////// CENTER
        tm = new  DefaultTableModel();
        table = new JTable(tm);
        tm.addColumn(" #");
        tm.addColumn("RESULTS");
        JScrollPane jsp = new JScrollPane(table);
        add(jsp);
        mainPanel.add(jsp, BorderLayout.CENTER);

        /////////////////// SOUTH
        JPanel southPanel = new JPanel();
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        southPanel.add(cbREGEX);
        southPanel.add(btClick);
        btClick.addActionListener( e ->{
            try{
                GameOn(e) ;
            }
            catch(Exception eee)
            {
                JOptionPane.showMessageDialog( null, eee.getMessage());
            }
        });
        
        bt = new JButton("Reset"); // add button that clears table results
        bt.addActionListener(e -> resetResults());
        southPanel.add(bt);

        btSaveToFile = new JButton("Save to file"); //add button to save the results to a file
        southPanel.add(btSaveToFile);
        btSaveToFile.addActionListener(e -> {
            try{
                SaveTableToFile(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        
        //add(southPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setTitle("Scrape the Internet");
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        Scrape s = new Scrape();
    }
}