using Finance.Models.Enums;

namespace Finance.Models.Models
{
    public class LoginResult
    {
        public LoginResult()
        {

        }
        public string Token { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Email { get; set; }
        public int Age { get; set; }
        public UserGender Gender { get; set; }
        public decimal DailyAllowance { get; set; } = 0m;
        public decimal Savings { get; set; } = 0m;
    }
}
