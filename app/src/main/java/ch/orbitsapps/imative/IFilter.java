package ch.orbitsapps.imative;

public interface IFilter {
    ImageData[] applyTo(ImageData data) throws Exception;
}
