package Engine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Utility class that creates UUIDv3 (MD5) and UUIDv5 (SHA1).
 * Code borrowed with appreciation from
 *   https://stackoverflow.com/questions/29059530/is-there-any-way-to-generate-the-same-uuid-from-a-string/63087679#63087679
 */
public class UuidGenerator
{
  private static final int VERSION_3 = 3; // UUIDv3 MD5
  private static final int VERSION_5 = 5; // UUIDv5 SHA1

  private static final String MESSAGE_DIGEST_MD5 = "MD5"; // UUIDv3
  private static final String MESSAGE_DIGEST_SHA1 = "SHA-1"; // UUIDv5

  private static UUID generateUuid(UUID namespace, String name, int version)
  {

    final byte[] hash;
    final MessageDigest hasher;

    // Choose algorithm based on UUID type and instantiate a MessageDigest.
    String algorithm = (version == 3) ? MESSAGE_DIGEST_MD5 : MESSAGE_DIGEST_SHA1;
    try
    {
      hasher = MessageDigest.getInstance(algorithm);

      // Insert name space if NOT NULL
      if( namespace != null )
      {
        hasher.update(toBytes(namespace.getMostSignificantBits()));
        hasher.update(toBytes(namespace.getLeastSignificantBits()));
      }

      // Generate the hash
      hash = hasher.digest(name.getBytes(StandardCharsets.UTF_8));

      // Split the hash into two parts: MSB and LSB
      long msb = toNumber(hash, 0, 8); // first 8 bytes for MSB
      long lsb = toNumber(hash, 8, 16); // last 8 bytes for LSB

      // Apply version and variant bits (required for RFC-4122 compliance)
      msb = (msb & 0xffffffffffff0fffL) | (version & 0x0f) << 12; // apply version bits
      lsb = (lsb & 0x3fffffffffffffffL) | 0x8000000000000000L; // apply variant bits

      // Return the UUID
      return new UUID(msb, lsb);

    }
    catch (NoSuchAlgorithmException e)
    {
      throw new RuntimeException(String.format("Message digest algorithm '%s' not supported.", algorithm));
    }
  }

  public static UUID md5Uuid(String string)
  {
    return generateUuid(null, string, VERSION_3);
  }

  public static UUID sha1Uuid(String string)
  {
    return generateUuid(null, string, VERSION_5);
  }

  public static UUID md5Uuid(UUID namespace, String string)
  {
    return generateUuid(namespace, string, VERSION_3);
  }

  public static UUID sha1Uuid(UUID namespace, String string)
  {
    return generateUuid(namespace, string, VERSION_5);
  }

  public static UUID randomUuid()
  {
    return UUID.randomUUID();
  }

  private static byte[] toBytes(final long number)
  {
    return new byte[] { (byte) (number >>> 56), (byte) (number >>> 48), (byte) (number >>> 40), (byte) (number >>> 32),
        (byte) (number >>> 24), (byte) (number >>> 16), (byte) (number >>> 8), (byte) (number) };
  }

  private static long toNumber(final byte[] bytes, final int start, final int length)
  {
    long result = 0;
    for( int i = start; i < length; i++ )
    {
      result = (result << 8) | (bytes[i] & 0xff);
    }
    return result;
  }

  /**
   * For tests!
   */
  public static void main(String[] args)
  {

    String string = "JUST_A_TEST_STRING";
    UUID namespace = UUID.randomUUID(); // A custom name space

    System.out.println("Java's generator");
    System.out.println("UUID.nameUUIDFromBytes():      '" + UUID.nameUUIDFromBytes(string.getBytes()) + "'");
    System.out.println();
    System.out.println("This generator");
    System.out.println("HashUuidCreator.Md5Uuid():  '" + UuidGenerator.md5Uuid(string) + "'");
    System.out.println("HashUuidCreator.Sha1Uuid(): '" + UuidGenerator.sha1Uuid(string) + "'");
    System.out.println();
    System.out.println("This generator WITH name space");
    System.out.println("HashUuidCreator.Md5Uuid():  '" + UuidGenerator.md5Uuid(namespace, string) + "'");
    System.out.println("HashUuidCreator.Sha1Uuid(): '" + UuidGenerator.sha1Uuid(namespace, string) + "'");
  }
}
