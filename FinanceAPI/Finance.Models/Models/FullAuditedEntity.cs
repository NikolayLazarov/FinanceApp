using System.ComponentModel.DataAnnotations;

namespace Finance.Models.Models
{
    public abstract class FullAuditedEntity
    {
        [Required]
        public int CreatorUserId { get; set; }
        [Required]
        public DateTime CreationDate { get; set; }
        public DateTime? LastModified { get; set; }
        public int? DeleterUserId { get; set; }
        public DateTime? DeletionTime { get; set; }
    }
}
