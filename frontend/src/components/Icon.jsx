import {
  BedDouble,
  Bath,
  Scaling,
  House,
  Building2,
  Warehouse,
  CarFront,
  WavesLadder,
  MapPin,
  KeyRound,
  Heart,
  Search,
  UserRound,
  SlidersHorizontal,
  ArrowRight,
  ShieldCheck,
  CircleHelp,
} from "lucide-react";

const ICONS = {
  bed: BedDouble,
  bath: Bath,
  area: Scaling,
  home: House,
  building: Building2,
  garage: Warehouse,
  car: CarFront,
  pool: WavesLadder,
  location: MapPin,
  key: KeyRound,
  heart: Heart,
  search: Search,
  user: UserRound,
  filter: SlidersHorizontal,
  arrowRight: ArrowRight,
  shield: ShieldCheck,
  question: CircleHelp,
};

export default function Icon({
  name,
  size = 24,
  strokeWidth = 1.6,
  label,
  ...props
}) {
  const IconComponent = ICONS[name] ?? CircleHelp;

  return (
    <IconComponent
      {...props}
      size={size}
      strokeWidth={strokeWidth}
      aria-hidden={label ? undefined : true}
      aria-label={label}
      role={label ? "img" : undefined}
    />
  );
}
