using AutoMapper;
using Finance.Models.DTOs;
using Finance.Models.Models;

namespace Finance.Models.Profiles
{
    public class ExpenseProfile : Profile
    {
        public ExpenseProfile()
        {
            CreateMap<CreateOrUpdateExpenseInput, Expense>()
                .ForMember(dest => dest.Id, src => src.MapFrom(e => e.Id))
                .ForMember(dest => dest.Title, src => src.MapFrom(e => e.Title))
                .ForMember(dest => dest.Date, src => src.MapFrom(e => e.Date))
                .ForMember(dest => dest.Category, src => src.MapFrom(e => e.Category))
                .ForMember(dest => dest.Amount, src => src.MapFrom(e => e.Amount));


        }
    }
}
