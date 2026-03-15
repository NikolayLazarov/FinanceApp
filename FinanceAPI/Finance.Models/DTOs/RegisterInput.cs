using Finance.Models.Enums;

namespace Finance.Models.DTOs
{
    public class RegisterInput
    {
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public int Age { get; set; }
        public UserGender Gender { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
    }
}
