import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

public class WeatherAppGui extends JFrame {

    private JSONObject weatherData;

    public WeatherAppGui(){
        // montar o GUI e adicionar um título
        super("Clima App");

        // configurar o GUI para encerrar os processos do programa quando o mesmo for fechado
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // tamanho do GUI (Pixels)
        setSize(450, 650);

        // iniciar no centro da tela
        setLocationRelativeTo(null);

        // layout manager null para posicionar manualmente os componentes dentro da interface
        setLayout(null);

        // bloquear redimensionamentos na interface
        setResizable(false);

        addGuiComponentes();
    }

    private void addGuiComponentes(){
        // campo de busca
        JTextField searchTextField = new JTextField();

        // setar a localização e o tamanho do campo
        searchTextField.setBounds(15, 15, 350, 45);

        // mudar a fonte e o tamanho
        searchTextField.setFont(new Font("Fira Code Retina", Font.PLAIN, 24));

        add(searchTextField);

        // imagem do clima
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // texto temperatura
        JLabel temperatureText = new JLabel("18 °C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // centralizar o texto
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //descrição do clima
        JLabel weatherCOnditionDesc = new JLabel("Nublado");
        weatherCOnditionDesc.setBounds(0, 405, 450, 36);
        weatherCOnditionDesc.setFont(new Font("Fira Code", Font.PLAIN, 32));
        weatherCOnditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherCOnditionDesc);


        // imagem humidade
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // texto humidade
        JLabel humidityText = new JLabel("<html><b>Humidade</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont( new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);


        // imagem velocidade do vento
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // texto velocidade do vento
        JLabel windspeedText = new JLabel("<html><b>Vento</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);


        // botão de busca
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // mudar o cursor on hover
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                // obter a localização do usuário
                String userInput = searchTextField.getText();

                // validar a entrada - remover espaços em branco
                if(userInput.replaceAll("\\s", "").length() <= 0 ){
                    return;
                }

                // recupere os dados climáticos
                weatherData = WeatherApp.getWeatherData(userInput);
//                System.out.println(weatherData);

                // atualize a interface

                // atualize a imagem do clima
                assert weatherData != null;
                String weatherCondition = (String) weatherData.get("weather_condition");

                // dependendo da condição vamos atualizar a imagem que corresponda à condição
                switch (weatherCondition){
                    case "Limpo":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Nublado":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Chuvoso":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Neve":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }
                // atualizar o texto de temperatura
                double temperature = (double)  weatherData.get("temperature");
                temperatureText.setText(temperature + " °C");

                // atualizar texto da condição
                weatherCOnditionDesc.setText(weatherCondition);

                //atualizar texto da humidade
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidade</b> " + humidity + "%</html>");

                // atualizar texto velocidade do vento
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Velocidade do vento</b> " + windspeed + "km//h</html>");



            }
        });
        add(searchButton);

    }

    // cria as imagens nos componentes da GUI
    private ImageIcon loadImage(String resourcePath) {
        try{
            //lê o arquivo de imagem do caminho dado
            BufferedImage image = ImageIO.read(new File(resourcePath));

            return new ImageIcon(image);
        } catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Recurso não encontrado");
        return null;
    }
}
