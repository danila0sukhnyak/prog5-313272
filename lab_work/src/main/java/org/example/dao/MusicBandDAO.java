package org.example.dao;

import org.example.enums.SortStatus;
import org.example.exception.MusicBandNotFoundException;
import org.example.exception.MusicBandWrongAttributeException;
import org.example.model.DataStorage;
import org.example.model.MusicBand;
import org.example.model.Person;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Реализация объекта доступа к данным (Data Access Object)
 * В качестве хранилища используется {@link ArrayList}
 * Хранит {@link HashSet} паспортных данных для проверки уникальности,
 * дату инициализации, статус сортировки {@link SortStatus}
 */
public class MusicBandDAO implements IMusicBandDAO {
    List<MusicBand> musicBandList = new ArrayList<>();
    Set<String> passportIDUniqueValues = new HashSet<>();
    LocalDateTime initDate = LocalDateTime.now();
    String type = "ArrayList";
    SortStatus sortStatus = SortStatus.ASC;

    /**
     * Проверяет объект на null, {@code passportID} на уникальность,
     * в случае успешной проверки добавляет объект в коллекцию
     *
     * @param musicBand объект для сохранения
     * @throws MusicBandWrongAttributeException при наличии невалидных аттрибутов
     */
    @Override
    public void save(MusicBand musicBand) {
        if (musicBand == null || musicBand.getFrontMan() == null) {
            throw new MusicBandWrongAttributeException("Значение поля не должно быть null");
        }
        String passportID = musicBand.getFrontMan().getPassportID();
        if (isPassportIDExists(passportID)) {
            throw new MusicBandWrongAttributeException("Значение этого поля должно быть уникальным");
        } else if (passportID != null) {
            passportIDUniqueValues.add(passportID);
        }
        musicBandList.add(musicBand);
    }

    @Override
    public List<MusicBand> getAll() {
        return musicBandList;
    }

    /**
     * Находит объект по {@code id} и меняет его на переданный
     * Проверяет уникальность {@code passportID} нового объекта, если он отличается от старого
     *
     * @param musicBand новый объект
     * @param id        id объекта для замены
     * @throws MusicBandWrongAttributeException при наличии невалидных аттрибутов
     */
    @Override
    public void update(MusicBand musicBand, Long id) {
        int number = getNumberById(id);
        MusicBand oldBand = musicBandList.get(number);
        Person oldFrontMan = musicBandList.get(number).getFrontMan();
        String passportID = musicBand.getFrontMan().getPassportID();
        if (passportID != null &&
                oldFrontMan != null &&
                !passportID.equals(oldFrontMan.getPassportID()) &&
                isPassportIDExists(passportID)) {
            throw new MusicBandWrongAttributeException("Значение этого поля должно быть уникальным");
        } else if (passportID != null) {
            passportIDUniqueValues.add(passportID);
            clearPassportId(oldBand);
        }
        musicBandList.set(number, musicBand);
    }

    @Override
    public MusicBand removeById(Long id) {
        int number = getNumberById(id);
        if (musicBandList.isEmpty() || musicBandList.size() - 1 < number) {
            return null;
        } else {
            MusicBand remove = musicBandList.remove(number);
            clearPassportId(remove);
            return remove;
        }
    }

    @Override
    public List<MusicBand> removeByDescription(String description) {
        List<MusicBand> removed = new ArrayList<>();
        Iterator<MusicBand> it = musicBandList.iterator();
        while (it.hasNext()) {
            MusicBand musicBand = it.next();
            if (musicBand != null &&
                    musicBand.getDescription() != null &&
                    musicBand.getDescription().equalsIgnoreCase(description)) {
                removed.add(musicBand);
                it.remove();
            }
        }
        return removed;
    }

    @Override
    public void clear() {
        musicBandList.clear();
        passportIDUniqueValues.clear();
    }

    @Override
    public boolean isPassportIDExists(String passportID) {
        return passportID != null && passportIDUniqueValues.contains(passportID);
    }

    @Override
    public int getNumberById(Long id) {
        for (int i = 0; i < musicBandList.size(); i++) {
            if (musicBandList.get(i).getId().equals(id)) {
                return i;
            }
        }
        throw new MusicBandNotFoundException("Объект не найден");
    }

    @Override
    public MusicBand getByNumber(int number) {
        return musicBandList.get(number);
    }

    @Override
    public MusicBand getById(Long id) {
        int numberById = getNumberById(id);
        return getByNumber(numberById);
    }

    @Override
    public void init(DataStorage dataStorage) {
        if (dataStorage.getInitDate() != null)
            this.initDate = dataStorage.getInitDate();
        List<MusicBand> bands = dataStorage.getBands();
        for (MusicBand band : bands) {
            save(band);
        }
    }

    @Override
    public DataStorage getData() {
        return new DataStorage(musicBandList, initDate, type);
    }

    @Override
    public void checkPassportIDUnique(String passportID) {
        if (isPassportIDExists(passportID)) {
            throw new MusicBandWrongAttributeException("Значение этого поля должно быть уникальным");
        }
    }

    @Override
    public MusicBand getMinimal() {
        List<MusicBand> newList = new ArrayList<>(musicBandList);
        Collections.sort(newList);
        if (newList.isEmpty()) {
            return null;
        }
        return newList.get(0);
    }

    @Override
    public MusicBand getByMaxId() {
        List<MusicBand> newList = new ArrayList<>(musicBandList);
        newList.sort(Comparator.comparingLong(MusicBand::getId));
        if (newList.isEmpty()) {
            return null;
        }
        return newList.get(newList.size() - 1);
    }

    @Override
    public MusicBand removeLast() {
        if (musicBandList.isEmpty()) {
            return null;
        } else {
            MusicBand removed = musicBandList.remove(musicBandList.size() - 1);
            clearPassportId(removed);
            return removed;
        }
    }

    private void clearPassportId(MusicBand remove) {
        if (remove != null && remove.getFrontMan() != null) {
            passportIDUniqueValues.remove(remove.getFrontMan().getPassportID());
        }
    }

    @Override
    public SortStatus reorder() {
        switch (sortStatus) {
            case DESC:
                Collections.reverse(musicBandList);
                sortStatus = SortStatus.ASC;
                break;
            case ASC:
                Collections.sort(musicBandList);
                sortStatus = SortStatus.DESC;
                break;
            default:
                break;
        }
        return sortStatus;
    }

    @Override
    public List<MusicBand> filterByDescription(String description) {
        List<MusicBand> newList = new ArrayList<>();
        for (MusicBand musicBand : musicBandList) {
            if (musicBand != null &&
                    musicBand.getDescription() != null &&
                    musicBand.getDescription().compareTo(description) < 0) {
                newList.add(musicBand);
            }
        }
        return newList;
    }
}
