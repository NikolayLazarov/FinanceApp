using AutoMapper;
using Finance.Models.DTOs;
using Finance.Models.Models;

namespace Finance.Models.Profiles
{
    public class RegisterUserProfile : Profile
    {
        public RegisterUserProfile()
        {
            CreateMap<RegisterInput, ApplicationUser>()
                .ForMember(dest => dest.FirstName, src => src.MapFrom(e => e.FirstName))
                .ForMember(dest => dest.LastName, src => src.MapFrom(e => e.LastName))
                .ForMember(dest => dest.Age, src => src.MapFrom(e => e.Age))
                .ForMember(dest => dest.Email, src => src.MapFrom(e => e.Email))
                .ForMember(dest => dest.UserName, src => src.MapFrom(e => e.Email))
                .ForMember(dest => dest.Gender, src => src.MapFrom(e => e.Gender));
        }
    }
}
