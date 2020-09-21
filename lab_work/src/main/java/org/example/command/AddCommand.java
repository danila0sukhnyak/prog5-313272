package org.example.command;

import org.example.enums.Color;
import org.example.enums.MusicGenre;
import org.example.exception.InterruptInputException;
import org.example.model.Coordinates;
import org.example.model.MusicBand;
import org.example.model.Person;
import org.example.util.MusicBandAttributeSetter;

import java.util.Arrays;

public class AddCommand extends AbstractCommand {

    @Override
    public String command() {
        return "add";
    }

    @Override
    public String description() {
        return "Добавить новый элемент в коллекцию";
    }

    /**
     * Использует {@link MusicBandAttributeSetter} для наполнения объекта {@link MusicBand}
     * и добавляет его в коллекцию
     * @param args аргументы
     */
    @Override
    public void execute(String[] args) {
        MusicBand musicBand = new MusicBand();
        musicBand.setFrontMan(new Person());
        musicBand.setCoordinates(new Coordinates());
        MusicBandAttributeSetter setter = new MusicBandAttributeSetter(consoleService);
        try {
            setter.setAttribute(musicBand,
                    "Введите название группы",
                    s -> s.setName(consoleService.read()));
            setter.setAttribute(musicBand,
                    "Введите количество участников",
                    s -> s.setNumberOfParticipants(Integer.valueOf(consoleService.read())));
            setter.setAttribute(musicBand,
                    "Введите описание группы",
                    s -> s.setDescription(consoleService.read()));
            setter.setAttribute(musicBand,
                    String.format("Введите жанр группы %s", Arrays.toString(MusicGenre.values())),
                    s -> s.setGenre(consoleService.read()));
            setter.setAttribute(musicBand,
                    "Введите координату x",
                    s -> s.getCoordinates().setX(Long.valueOf(consoleService.read())));
            setter.setAttribute(musicBand,
                    "Введите координату y",
                    s -> s.getCoordinates().setY(Float.valueOf(consoleService.read())));
            setter.setAttribute(musicBand,
                    "Введите имя солиста",
                    s -> s.getFrontMan().setName(consoleService.read()));
            setter.setAttribute(musicBand,
                    "Введите рост солиста",
                    s -> s.getFrontMan().setHeight(Double.valueOf(consoleService.read())));
            setter.setAttribute(musicBand,
                    String.format("Введите цвет глаз солиста %s", Arrays.toString(Color.values())),
                    s -> s.getFrontMan().setEyeColor(consoleService.read()));
            setter.setAttribute(musicBand,
                    "Введите номер паспорта солиста",
                    s -> {
                        String line = consoleService.read();
                        musicBandDAO.checkPassportIDUnique(line);
                        s.getFrontMan().setPassportID(line);
                    });
        } catch (InterruptInputException e) {
            consoleService.printLn("Добавление элемента прервано");
            return;
        }
        musicBandDAO.save(musicBand);
        consoleService.printLn("Элемент успешно добавлен!");
    }
}
