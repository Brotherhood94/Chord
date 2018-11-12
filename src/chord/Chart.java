package chord;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.*;
import java.awt.*;
import java.io.*;


public class Chart extends ApplicationFrame {

    private JFreeChart chart = null;
    private String title = null;
    private DefaultCategoryDataset dataset = null;
    private CategoryPlot plot = null;
    public Chart(String title, String xLabel, String yLabel, Color color){
        super("ChordSimulation");
        this.title = title;
        this.dataset = new DefaultCategoryDataset();
        this.chart = createChart(dataset, title, xLabel, yLabel, color);
        this.plot = this.chart.getCategoryPlot();
        ChartPanel chartPanel = new ChartPanel(chart);
        this.setSize(new java.awt.Dimension(1280, 720));
        this.setContentPane(chartPanel);
    }

    public void addToDataset(Integer value, String type, String xlabel){
        dataset.addValue(value, type, xlabel);
    }

    public void saveChart(String file){
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
        try {
            ChartUtilities.saveChartAsPNG(new File(file), chart, this.getWidth(), this.getHeight());
        } catch (IllegalStateException e){
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset,String title, String xLabel, String yLabel, Color color){
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false,true,false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        ((BarRenderer)plot.getRenderer()).setBarPainter(new StandardBarPainter());
        BarRenderer r = (BarRenderer)chart.getCategoryPlot().getRenderer();
        r.setSeriesPaint(0, color);
        return chart;
    }



    public void addMarker(double value, String label, Color color){
        final Marker target = new ValueMarker(value);
        target.setPaint(color);
        target.setStroke(new BasicStroke(3.0f));
        target.setLabel(label);
        target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        this.plot.addRangeMarker(target);
    }

}
