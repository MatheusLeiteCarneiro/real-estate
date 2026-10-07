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
  ArrowLeft,
  ShieldCheck,
  CircleHelp,
  TriangleAlert,
  LoaderCircle,
  SearchX,
  ImageOff,
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
  arrowLeft: ArrowLeft,
  shield: ShieldCheck,
  question: CircleHelp,
  alert: TriangleAlert,
  loader: LoaderCircle,
  searchOff: SearchX,
  imageOff: ImageOff,
};

export default function Icon({
  name,
  size = "1.5rem",
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
