package com.system;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Window window = new Window();
        window.construct();
        try {
            window.show();
            window.lockFields(true); window.lockActionButtons(true); window.lockNavButtons(true);

            window.showLoadingDialog();
                window.loadRecords();
                window.modeEdit(Window.OPERATION_CREATE);
            window.hideLoadingDialog();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(null, "Ocorreu um problema interno. O aplicativo será fechado.",
                "Erro Interno", JOptionPane.ERROR_MESSAGE);
            ise.printStackTrace();
            System.exit(1);
        }
    }

    static class Window implements ActionListener {
        private JFrame frame;
        private JDialog frameLoading;

        private JTextField txfName;
        private JTextField txfCPF;
        private JTextField txfRG;
        private JTextField txfEmail;
        private JTextField txfPhone;
        private JTextField txfCEP;

        private JButton btnAdd;
        private JButton btnDelete;
        private JButton btnEdit;
        private JButton btnClear;
        private JButton btnSearch;

        private JButton btnFirst;
        private JButton btnPrevious;
        private JButton btnNext;
        private JButton btnLast;

        private final String ACTION_CLEAR       = "CL";
        private final String ACTION_ADD         = "AD";
        private final String ACTION_DELETE      = "DL";
        private final String ACTION_EDIT        = "ED";
        private final String ACTION_SEARCH      = "SR";
        private final String ACTION_FIRST       = "FR";
        private final String ACTION_PREVIOUS    = "PV";
        private final String ACTION_NEXT        = "NX";
        private final String ACTION_LAST        = "LT";

        public static final byte OPERATION_VIEW = 0b0;
        public static byte OPERATION_CREATE     = 0b1;
        public static byte OPERATION_UPDATE     = 0b10;
        public static byte OPERATION_SEARCH     = 0b11;

        private byte operation = OPERATION_CREATE;
        private ArrayList<Model> clients;
        private int currentClientIndex = -1;
        private Model lastSearch;
        private Model searchString;
        private int currentSearchOccurrence = 0;

        public void construct() {
            // Client Frame
            frame = new JFrame("Cliente");
            frame.setLocation(0, 0);
            frame.setPreferredSize(new Dimension(640 + 16, 480 + 40));
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(null);

            // Loading Frame
            frameLoading = new JDialog(frame, "Carregando", true);
            frameLoading.setModal(false);
            frameLoading.setPreferredSize(new Dimension(300 + 16, 112 + 40));
            frameLoading.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            JPanel pnlContainer = new JPanel();
            pnlContainer.setLayout(null);

            JLabel lblLoading = new JLabel("Carregando...");
            lblLoading.setLocation(16, 16); lblLoading.setSize(268, 16);
            pnlContainer.add(lblLoading);

            JProgressBar pgbProgress = new JProgressBar();
            pgbProgress.setLocation(16, 48); pgbProgress.setSize(268, 20);
            pgbProgress.setIndeterminate(true);
            pnlContainer.add(pgbProgress);

            frameLoading.getContentPane().add(pnlContainer);
            frameLoading.pack();

            // Client Frame Content
            // Name
            JLabel lblName = new JLabel("Nome: *");
            lblName.setLocation(16, 16); lblName.setSize(300, 16);
            frame.add(lblName);

            txfName = new JTextField();
            txfName.setLocation(16, 40); txfName.setSize(300, 27);
            frame.add(txfName);

            // CPF
            JLabel lblCPF = new JLabel("CPF:");
            lblCPF.setLocation(16, 75); lblCPF.setSize(300, 16);
            frame.add(lblCPF);

            txfCPF = new JTextField();
            txfCPF.setLocation(16, 99); txfCPF.setSize(170, 27);
            frame.add(txfCPF);

            // RG
            JLabel lblRG = new JLabel("RG:");
            lblRG.setLocation(16, 134); lblRG.setSize(300, 16);
            frame.add(lblRG);

            txfRG = new JTextField();
            txfRG.setLocation(16, 158); txfRG.setSize(170, 27);
            frame.add(txfRG);

            // Email
            JLabel lblEmail = new JLabel("Email:");
            lblEmail.setLocation(16, 193); lblEmail.setSize(300, 16);
            frame.add(lblEmail);

            txfEmail = new JTextField();
            txfEmail.setLocation(16, 217); txfEmail.setSize(300, 27);
            frame.add(txfEmail);

            // Phone
            JLabel lblTelefone = new JLabel("Telefone:");
            lblTelefone.setLocation(16, 252); lblTelefone.setSize(300, 16);
            frame.add(lblTelefone);

            txfPhone = new JTextField();
            txfPhone.setLocation(16, 276); txfPhone.setSize(170, 27);
            frame.add(txfPhone);

            // CEP
            JLabel lblCEP = new JLabel("CEP:");
            lblCEP.setLocation(16, 311); lblCEP.setSize(300, 16);
            frame.add(lblCEP);

            txfCEP = new JTextField();
            txfCEP.setLocation(16, 335); txfCEP.setSize(170, 27);
            frame.add(txfCEP);


            // Action Buttons
            btnAdd = new JButton("Adicionar");
            btnAdd.setLocation(478, 40); btnAdd.setSize(130, 43);
            btnAdd.setActionCommand(ACTION_ADD);
            btnAdd.addActionListener(this);
            frame.add(btnAdd);

            btnDelete = new JButton("Excluir");
            btnDelete.setLocation(478, 91); btnDelete.setSize(130, 43);
            btnDelete.setActionCommand(ACTION_DELETE);
            btnDelete.addActionListener(this);
            frame.add(btnDelete);

            btnEdit = new JButton("Editar");
            btnEdit.setLocation(478, 142); btnEdit.setSize(130, 43);
            btnEdit.setActionCommand(ACTION_EDIT);
            btnEdit.addActionListener(this);
            frame.add(btnEdit);

            btnClear = new JButton("Limpar");
            btnClear.setLocation(478, 193); btnClear.setSize(130, 43);
            btnClear.setActionCommand(ACTION_CLEAR);
            btnClear.addActionListener(this);
            frame.add(btnClear);

            btnSearch = new JButton("Pesquisar");
            btnSearch.setLocation(478, 244); btnSearch.setSize(130, 43);
            btnSearch.setActionCommand(ACTION_SEARCH);
            btnSearch.addActionListener(this);
            frame.add(btnSearch);

            // Navigation Buttons
            btnFirst = new JButton("|<<");
            btnFirst.setLocation(364, 421); btnFirst.setSize(55, 43);
            btnFirst.setActionCommand(ACTION_FIRST);
            btnFirst.addActionListener(this);
            frame.add(btnFirst);

            btnPrevious = new JButton("<<");
            btnPrevious.setLocation(427, 421); btnPrevious.setSize(55, 43);
            btnPrevious.setActionCommand(ACTION_PREVIOUS);
            btnPrevious.addActionListener(this);
            frame.add(btnPrevious);

            btnNext = new JButton(">>");
            btnNext.setLocation(490, 421); btnNext.setSize(55, 43);
            btnNext.setActionCommand(ACTION_NEXT);
            btnNext.addActionListener(this);
            frame.add(btnNext);

            btnLast = new JButton(">>|");
            btnLast.setLocation(553, 421); btnLast.setSize(55, 43);
            btnLast.setActionCommand(ACTION_LAST);
            btnLast.addActionListener(this);
            frame.add(btnLast);

            frame.pack();
        }

        public void show() throws IllegalStateException {
            if (frame == null)
                throw new IllegalStateException("Call the method construct first.");

            frame.setVisible(true);
        }
        public void showLoadingDialog() {
            if (frameLoading == null)
              throw new IllegalStateException("Call the method construct first.");

            frameLoading.setVisible(true);
        }
        public void hideLoadingDialog() {
            if (frameLoading == null)
              throw new IllegalStateException("Call the method construct first.");

            frameLoading.setVisible(false);
        }

        public void lockFields(boolean lock) {
            txfName.setEnabled(!lock);
            txfCPF.setEnabled(!lock);
            txfRG.setEnabled(!lock);
            txfEmail.setEnabled(!lock);
            txfPhone.setEnabled(!lock);
            txfCEP.setEnabled(!lock);
        }
        public void lockActionButtons(boolean lock) {
            btnAdd.setEnabled(!lock);
            btnDelete.setEnabled(!lock);
            btnEdit.setEnabled(!lock);
            btnClear.setEnabled(!lock);
            btnSearch.setEnabled(!lock);
        }
        public void lockNavButtons(boolean lock) {
            btnFirst.setEnabled(!lock);
            btnPrevious.setEnabled(!lock);
            btnNext.setEnabled(!lock);
            btnLast.setEnabled(!lock);
        }

        public void clearFields() {
            txfName.setText("");
            txfCPF.setText("");
            txfRG.setText("");
            txfEmail.setText("");
            txfPhone.setText("");
            txfCEP.setText("");

            txfName.requestFocus();
        }

        public void actionPerformed(ActionEvent event) {
            switch (event.getActionCommand()) {
                case ACTION_CLEAR: {
                    clearFields();
                } break;
                case ACTION_ADD: {
                    if (operation == OPERATION_CREATE) {
                        saveRecord();
                        clearFields();
                        loadRecords();
                        modeEdit(OPERATION_CREATE);
                    }
                    else if (operation == OPERATION_VIEW) {
                        clearFields();
                        modeEdit(OPERATION_CREATE);
                    }
                } break;
                case ACTION_DELETE: {
                    deleteRecord();
                    loadRecords();
                    goToLastRecord();
                } break;
                case ACTION_EDIT: {
                    if (operation == OPERATION_UPDATE) {
                        saveRecord();
                        modeView();
                    } else if (operation == OPERATION_VIEW) {
                        modeEdit(OPERATION_UPDATE);
                    }
                } break;
                case ACTION_SEARCH: {
                    if (operation == OPERATION_SEARCH) {
                        search();
                    } else {
                        modeSearch();
                    }
                } break;
                case ACTION_FIRST: {
                    resetSearch();
                    goToFirstRecord();
                } break;
                case ACTION_PREVIOUS: {
                    resetSearch();
                    goToPreviousRecord();
                } break;
                case ACTION_NEXT: {
                    resetSearch();
                    goToNextRecord();
                } break;
                case ACTION_LAST: {
                    resetSearch();
                    goToLastRecord();
                } break;
                default: {
                }
            }
        }

        private void resetSearch() {
            lastSearch = null;
            searchString = null;
            currentSearchOccurrence = 0;
        }

        private void search() {
            Model model = new Model();
            model.setName(txfName.getText());
            model.setCPF(txfCPF.getText());
            model.setRG(txfRG.getText());
            model.setEmail(txfEmail.getText());
            model.setPhone(txfPhone.getText());
            model.setCEP(txfCEP.getText());

            if (lastSearch != null) {
                if (!(model.getName().equals(lastSearch.getName())
                        && model.getCPF().equals(lastSearch.getCPF())
                        && model.getRG().equals(lastSearch.getRG())
                        && model.getEmail().equals(lastSearch.getEmail())
                        && model.getPhone().equals(lastSearch.getPhone())
                        && model.getCEP().equals(lastSearch.getCEP()))) {
                    resetSearch();
                    searchString = model;
                }
            } else {
                searchString = model;
            }

            try {
                model = Database.getInstance().search(searchString, currentSearchOccurrence);
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(null, "Falha ao pesquisar registro.",
                        "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
            }

            if (model == null) {
                JOptionPane.showMessageDialog(null, "Não existem registros com os termos pesquisados.",
                        "Pesquisa", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            currentSearchOccurrence++;

            txfName.setText(model.getName());
            txfCPF.setText(model.getCPF());
            txfRG.setText(model.getRG());
            txfEmail.setText(model.getEmail());
            txfPhone.setText(model.getPhone());
            txfCEP.setText(model.getCEP());
            lastSearch = model;
        }

        private void deleteRecord() {
            int reply = JOptionPane.showConfirmDialog(
                    null,
                    "Deseja deletar este cliente?",
                    "Deletar",
                    JOptionPane.YES_NO_OPTION);

            if (reply == JOptionPane.YES_OPTION) {
                try {
                    Database.getInstance().delete(clients.get(currentClientIndex).getId());
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Falha ao carregar registro.",
                            "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void goToFirstRecord() {
            modeView();
            currentClientIndex = 0;
            loadRecord(currentClientIndex);

            btnFirst.setEnabled(false);
            btnPrevious.setEnabled(false);
            if (clients.size() >= 2) {
               btnNext.setEnabled(true);
               btnLast.setEnabled(true);
            }
        }
        private void goToPreviousRecord() {
            modeView();
            currentClientIndex--;
            loadRecord(currentClientIndex);

            if (currentClientIndex == 0) {
                btnFirst.setEnabled(false);
                btnPrevious.setEnabled(false);
            }
            if (clients.size() >= 2 && currentClientIndex < clients.size() - 1) {
                btnNext.setEnabled(true);
                btnLast.setEnabled(true);
            }
        }
        private void goToNextRecord() {
            modeView();
            currentClientIndex++;
            loadRecord(currentClientIndex);

            if (currentClientIndex == (clients.size() - 1)) {
                btnNext.setEnabled(false);
                btnLast.setEnabled(false);
            }
            if (clients.size() >= 2 && currentClientIndex > 0) {
                btnPrevious.setEnabled(true);
                btnFirst.setEnabled(true);
            }
        }
        private void goToLastRecord() {
            modeView();
            currentClientIndex = clients.size() - 1;
            loadRecord(currentClientIndex);

            btnLast.setEnabled(false);
            btnNext.setEnabled(false);
            if (clients.size() > 1) {
                btnPrevious.setEnabled(true);
                btnFirst.setEnabled(true);
            }
        }

        public void modeView() {
            operation = OPERATION_VIEW;

            txfName.setEditable(false);
            txfCPF.setEditable(false);
            txfRG.setEditable(false);
            txfEmail.setEditable(false);
            txfPhone.setEditable(false);
            txfCEP.setEditable(false);
            btnClear.setEnabled(false);

            btnAdd.setEnabled(true);
            btnDelete.setEnabled(true);
            btnEdit.setEnabled(true);
        }
        public void modeEdit(byte mode) {
            lockFields(false);
            operation = mode;
            if (operation == OPERATION_CREATE)
                currentClientIndex = clients.size();

            txfName.setEditable(true);
            txfCPF.setEditable(true);
            txfRG.setEditable(true);
            txfEmail.setEditable(true);
            txfPhone.setEditable(true);
            txfCEP.setEditable(true);

            if (mode == OPERATION_CREATE) {
                btnAdd.setEnabled(true);
                btnEdit.setEnabled(false);
            }
            else if (mode == OPERATION_UPDATE) {
                btnAdd.setEnabled(false);
                btnEdit.setEnabled(true);
            }
            btnClear.setEnabled(true);

            if (clients.size() == 0) {
                btnDelete.setEnabled(false);
                btnSearch.setEnabled(false);

                if (mode == OPERATION_UPDATE)
                    return;

                lockNavButtons(true);
            } else if (clients.size() == 1) {
                btnDelete.setEnabled(false);
                btnSearch.setEnabled(true);

                if (mode == OPERATION_UPDATE)
                    return;

                btnFirst.setEnabled(true);
                btnPrevious.setEnabled(false);
                btnNext.setEnabled(false);
                btnLast.setEnabled(false);
            } else {
                btnDelete.setEnabled(false);
                btnSearch.setEnabled(true);

                if (mode == OPERATION_UPDATE)
                    return;

                btnFirst.setEnabled(true);
                btnPrevious.setEnabled(true);
                btnNext.setEnabled(false);
                btnLast.setEnabled(false);
            }
        }
        public void modeSearch() {
            goToLastRecord();

            operation = OPERATION_SEARCH;

            clearFields();
            txfName.setEditable(true);
            txfCPF.setEditable(true);
            txfRG.setEditable(true);
            txfEmail.setEditable(true);
            txfPhone.setEditable(true);
            txfCEP.setEditable(true);
            btnClear.setEnabled(true);

            btnAdd.setEnabled(false);
            btnDelete.setEnabled(false);
            btnEdit.setEnabled(false);
            btnSearch.setEnabled(true);

            txfName.requestFocus();
        }

        private void loadRecord(int index) {
            try {
                Model model = Database.getInstance().list(clients.get(index).getId());

                txfName.setText(model.getName());
                txfCPF.setText(model.getCPF());
                txfRG.setText(model.getRG());
                txfEmail.setText(model.getEmail());
                txfPhone.setText(model.getPhone());
                txfCEP.setText(model.getCEP());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Falha ao carregar registro.",
                        "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
            }
        }
        private void saveRecord() {
            if (!validateFields()) {
                JOptionPane.showMessageDialog(null, "Preencha os campos com o asterisco.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Model model = new Model();
            if (operation == OPERATION_UPDATE)
                model.setId(clients.get(currentClientIndex).getId());
            model.setName(txfName.getText());
            model.setCPF(txfCPF.getText());
            model.setRG(txfRG.getText());
            model.setEmail(txfEmail.getText());
            model.setPhone(txfPhone.getText());
            model.setCEP(txfCEP.getText());

            try {
                if (operation == OPERATION_CREATE)
                    Database.getInstance().insert(model);
                else if (operation == OPERATION_UPDATE)
                    Database.getInstance().update(model);
                JOptionPane.showMessageDialog(null, "O registro foi salvo com sucesso",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Houve um problema e o registro não foi salvo.",
                  "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
            }
        }
        private boolean validateFields() {
            if (txfName.getText().trim().length() == 0)
              return false;

            return true;
        }
        private void loadRecords() {
            try {
                clients = Database.getInstance().listAll();
            } catch (SQLException sqle) {
                hideLoadingDialog();
                JOptionPane.showMessageDialog(null, "Ocorreu um problema ao se comunicar com o banco de dados. O aplicativo será fechado.",
                        "Problema com o banco de dados", JOptionPane.ERROR_MESSAGE);
                sqle.printStackTrace();
                System.exit(1);
            }
        }
    }

    static class Model {
        private int id;
        private String name;
        private String cpf;
        private String rg;
        private String email;
        private String phone;
        private String cep;

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getCPF() {
            return cpf;
        }
        public void setCPF(String cpf) {
            this.cpf = cpf;
        }

        public String getRG() {
            return rg;
        }
        public void setRG(String rg) {
            this.rg = rg;
        }

        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCEP() {
            return cep;
        }
        public void setCEP(String cep) {
            this.cep = cep;
        }
    }

    static class Database {
        private static Database database;

        private final String URI_CONNECTION = "jdbc:mariadb://localhost:3306/Atividade3";
        private final String USERNAME = "root";
        private final String PASSWORD = "";

        private Connection connection;

        public static Database getInstance() throws SQLException {
            if (database == null)
                database = new Database();

            return database;
        }

        private Database() throws SQLException {
            connection = DriverManager.getConnection(
                URI_CONNECTION, USERNAME, PASSWORD);
        }

        public void insert(Model model) throws SQLException {
            String sql = "INSERT Client VALUES (NULL, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, model.getName());
            stmt.setString(2, model.getCPF());
            stmt.setString(3, model.getRG());
            stmt.setString(4, model.getEmail());
            stmt.setString(5, model.getPhone());
            stmt.setString(6, model.getCEP());

            stmt.executeUpdate();
        }

        public void update(Model model) throws SQLException {
            String sql = "UPDATE Client SET Name = ?, Cpf = ?, Rg = ?, Email = ?, Phone = ?, Cep = ? WHERE Id = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, model.getName());
            stmt.setString(2, model.getCPF());
            stmt.setString(3, model.getRG());
            stmt.setString(4, model.getEmail());
            stmt.setString(5, model.getPhone());
            stmt.setString(6, model.getCEP());
            stmt.setInt(7, model.getId());

            stmt.executeUpdate();
        }

        public void delete(int id) throws SQLException {
            String sql = "DELETE FROM Client WHERE Id = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);

            stmt.executeUpdate();
        }

        public Model list(int id) throws SQLException {
            String sql = "SELECT Id, Name, Cpf, Rg, Email, Phone, Cep FROM Client WHERE Id = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();

            if (result.first()) {
                Model model = new Model();
                model.setId(result.getInt("Id"));
                model.setName(result.getString("Name"));
                model.setCPF(result.getString("Cpf"));
                model.setRG(result.getString("Rg"));
                model.setEmail(result.getString("Email"));
                model.setPhone(result.getString("Phone"));
                model.setCEP(result.getString("Cep"));

                return model;
            }

            return null;
        }

        public ArrayList<Model> listAll() throws SQLException {
            String sql = "SELECT Id FROM Client";

            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(sql);

            ArrayList<Model> dados = new ArrayList<Model>();
            while (result.next()) {
                Model model = new Model();
                model.setId(result.getInt("Id"));

                dados.add(model);
            }

            return dados;
        }

        public Model search(Model model, int currentSearchOccurrence) throws SQLException {
            String sql = "SELECT Id, Name, Cpf, Rg, Email, Phone, Cep FROM Client " +
                    "WHERE Name LIKE ? OR Cpf LIKE ? OR Rg LIKE ? OR Email LIKE ? OR Phone LIKE ? OR Cep LIKE ? " +
                    "LIMIT 1 OFFSET ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, model.getName().equals("") ? "null" : "%" + model.getName() + "%");
            stmt.setString(2, model.getCPF().equals("") ? "null" : "%" + model.getCPF() + "%");
            stmt.setString(3, model.getRG().equals("") ? "null" : "%" + model.getRG() + "%");
            stmt.setString(4, model.getEmail().equals("") ? "null" : "%" + model.getEmail() + "%");
            stmt.setString(5, model.getPhone().equals("") ? "null" : "%" + model.getPhone() + "%");
            stmt.setString(6, model.getCEP().equals("") ? "null" : "%" + model.getCEP() + "%");
            stmt.setInt(7, currentSearchOccurrence);
            ResultSet result = stmt.executeQuery();

            if (result.first()) {
                Model clientModel = new Model();
                clientModel.setId(result.getInt("Id"));
                clientModel.setName(result.getString("Name"));
                clientModel.setCPF(result.getString("Cpf"));
                clientModel.setRG(result.getString("Rg"));
                clientModel.setEmail(result.getString("Email"));
                clientModel.setPhone(result.getString("Phone"));
                clientModel.setCEP(result.getString("Cep"));

                return clientModel;
            }

            return null;
        }
    }
}
