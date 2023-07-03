package develop;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class TableExample {
	public static void main(String[] args) {
		// Create a JFrame
		JFrame frame = new JFrame("Table Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create a JTable with custom table model
		DefaultTableModel model = new DefaultTableModel(10, 15); // 10 rows, 5 columns
		JTable table = new JTable(model);
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		model.setValueAt( 10, 9, 4 );

		// Create a JScrollPane and add the table to it
		JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );

//		// Set custom table header renderer
//		table.getTableHeader().setDefaultRenderer(new LeftAlignedHeaderRenderer(table));
//		table.setTableHeader( null );
//
//		// Set the row header (column headers on the left)
//		Vector<String> columnNames = new Vector<>();
//		for (int i = 0; i < model.getColumnCount(); i++) {
//			columnNames.add(model.getColumnName(i));
//		}
//		JList<String> rowHeader = new JList<>(columnNames);
//		rowHeader.setFixedCellWidth(100); // Adjust the width of the row header as needed
//		scrollPane.setRowHeaderView(rowHeader);

		// Add the scroll pane to the frame
		frame.getContentPane().add(scrollPane);

		// Set the size and make the frame visible
		frame.setSize(500, 300);
		frame.setVisible(true);
	}

	// Custom table header renderer to left-align the headers
	private static class LeftAlignedHeaderRenderer extends DefaultTableCellRenderer {
		public LeftAlignedHeaderRenderer(JTable table) {
			setHorizontalAlignment(SwingConstants.LEFT);
			setOpaque(true);
			setForeground(table.getTableHeader().getForeground());
			setBackground(table.getTableHeader().getBackground());
			setFont(table.getTableHeader().getFont());
		}
	}
}
