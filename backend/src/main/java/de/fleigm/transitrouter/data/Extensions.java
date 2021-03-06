package de.fleigm.transitrouter.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Extensions provide an easy way to add arbitrary data to an entity.
 * Every extension can only exists once per entity.
 */
public class Extensions {

  @JsonIgnore
  private final Map<Class<?>, Extension> extensions;

  /**
   * Create new empty extension container.
   */
  public Extensions() {
    this(new HashMap<>());
  }

  /**
   * Create a new extension container with the given extensions.
   * @param extensions extensions to add
   */
  public Extensions(Map<Class<?>, Extension> extensions) {
    this.extensions = new HashMap<>();
    extensions.forEach((type, extension) -> add(extension));
  }

  /**
   * Get an extension by it type.
   *
   * @param extension extension type
   * @param <T>       type
   * @return extension
   */
  @SuppressWarnings("unchecked")
  public <T extends Extension> Optional<T> get(Class<T> extension) {
    return (Optional<T>) Optional.ofNullable(extensions.get(extension));
  }

  /**
   * Get an extension by its type or create and add it if the extension is not present yet.
   *
   * @param extension       extension type
   * @param defaultSupplier supplier if extension does not exists yet.
   * @param <T>             type
   * @return extension
   */
  @SuppressWarnings("unchecked")
  public <T extends Extension> T getOrCreate(Class<T> extension, Supplier<T> defaultSupplier) {
    return (T) extensions.computeIfAbsent(extension, x -> defaultSupplier.get());
  }

  /**
   * Determine if an extension of a given type is present.
   *
   * @param extension extension type.
   * @param <T>       type
   * @return extension is present.
   */
  public <T> boolean has(Class<T> extension) {
    return extensions.containsKey(extension);
  }

  /**
   * Add a new extension.
   * It will overwrite any present extension of that type.
   *
   * @param extension new extension.
   * @param <T>       type
   * @return this
   */
  public <T extends Extension> Extensions add(T extension) {
    extensions.put(extension.getClass(), extension);
    return this;
  }

  /**
   * Returns a copy of the current inner status of the extensions.
   * Changes on the HashMap do not alter extensions instance.
   *
   * @return raw extensions.
   */
  @JsonAnyGetter
  public Map<Class<?>, Extension> unwrap() {
    return new HashMap<>(extensions);
  }

  @JsonAnySetter
  public void _jacksonAdd(String key, Extension extension) {
    add(extension);
  }
}
