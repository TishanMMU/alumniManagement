
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminManageOtherProfilesPanel extends JFrame {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton createBtn, editBtn, deleteBtn, refreshBtn, viewBtn;

    public AdminManageOtherProfilesPanel() {
        setTitle("Manage Other Profiles");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Manage Other User Profiles", SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 24));
        header.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
            "User ID", "First Name", "Last Name", "Email", "Role", "Phone", "Status"
        }, 0);
        userTable = new JTable(tableModel);
        userTable.setRowHeight(28);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        createBtn = new JButton("Create User");
        editBtn = new JButton("Edit User");
        deleteBtn = new JButton("Delete User");
        viewBtn = new JButton("View Profile");
        refreshBtn = new JButton("Refresh");

        JButton lockUnlockBtn = new JButton("Lock/Unlock");
        styleButton(lockUnlockBtn);
        buttonPanel.add(lockUnlockBtn);

        for (JButton btn : new JButton[]{createBtn, editBtn, deleteBtn, viewBtn, refreshBtn}) {
            styleButton(btn);
            buttonPanel.add(btn);
        }
        add(buttonPanel, BorderLayout.SOUTH);

        loadUserData();

        // Button Actions
        createBtn.addActionListener(e -> openCreateUserDialog());
        editBtn.addActionListener(e -> openEditUserDialog());
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadUserData());
        viewBtn.addActionListener(e -> openViewUserDialog());
        lockUnlockBtn.addActionListener(e -> toggleAccountStatus());

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Tahoma", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(150, 40));
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT userID, firstName, lastName, email, role, phone, accountStatus FROM User WHERE role != 'admin'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getBoolean("accountStatus") ? "Active" : "Locked";
                tableModel.addRow(new Object[]{
                    rs.getInt("userID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    status
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openCreateUserDialog() {
        UserFormDialog dialog = new UserFormDialog(null);
        dialog.setVisible(true);
        loadUserData();
    }

    private void openEditUserDialog() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }
        int userID = (int) tableModel.getValueAt(row, 0);
        UserFormDialog dialog = new UserFormDialog(userID);
        dialog.setVisible(true);
        loadUserData();
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }
        int userID = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM User WHERE userID = ?")) {
                    stmt.setInt(1, userID);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "User deleted successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                loadUserData();
            }
        }
    }

    private void openViewUserDialog() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to view.");
            return;
        }
        int userID = (int) tableModel.getValueAt(row, 0);
        new ViewUserProfilePanel(userID);
    }

    private void toggleAccountStatus() {
    int row = userTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a user to lock/unlock.");
        return;
    }

    int userID = (int) tableModel.getValueAt(row, 0);
    String currentStatus = (String) tableModel.getValueAt(row, 6); // Status column index

    boolean newStatus = currentStatus.equalsIgnoreCase("Locked"); // flip it

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE User SET accountStatus = ? WHERE userID = ?")) {
        stmt.setBoolean(1, newStatus);
        stmt.setInt(2, userID);
        stmt.executeUpdate();

        JOptionPane.showMessageDialog(this, "User has been " + (newStatus ? "unlocked." : "locked."));
        loadUserData(); // refresh table
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to update account status.");
    }
}

}
