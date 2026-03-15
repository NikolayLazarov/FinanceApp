using System.ComponentModel.DataAnnotations;

namespace Finance.Models.Models
{
    public abstract class FullAuditedEntity
    {
        [Required]
        public string CreatorUserId { get; set; }
        [Required]
        public DateTime CreationDate { get; set; }
        public DateTime? LastModified { get; set; }
        public string? DeleterUserId { get; set; }
        public DateTime? DeletionTime { get; set; }
    }
}
